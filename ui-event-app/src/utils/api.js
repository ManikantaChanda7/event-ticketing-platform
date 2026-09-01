// utils/api.js
import axios from "axios";
import { logout } from "../redux/slices/authSlice";
import { toastRef } from "../components/toastProvider";
import { getApiBaseUrl } from "../config/apiConfig";
import { performTokenRefresh, setRefreshStore } from "./tokenRefresh";

let storeRef = null; // will be set from store setup

export const setStore = (store) => {
  storeRef = store;
  setRefreshStore(store);
};

// Refresh token queue/mutex to prevent multiple concurrent refresh calls
let isRefreshing = false;
let refreshSubscribers = [];
let redirectInProgress = false; // Prevent multiple redirects

function addRefreshSubscriber(callback) {
  refreshSubscribers.push(callback);
}

function onRefreshed(token) {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
}

const api = axios.create({
  baseURL: getApiBaseUrl(),
  headers: { "Content-Type": "application/json" },
});

// Attach token automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const status = error.response?.status;
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      "Something went wrong";

    // Skip toast if _skipToast is true
    const skipToast = error.config?._skipToast;

    // ✅ REACTIVE: Handle token refresh when 401 received
    if (
      status === 401 &&
      !error.config?._retry &&
      localStorage.getItem("refreshToken")
    ) {
      console.log("🔴 401 received, attempting token refresh...");

      if (isRefreshing) {
        console.log("⏳ Refresh already in progress, queuing request...");
        // Wait for the ongoing refresh to complete
        return new Promise((resolve) => {
          addRefreshSubscriber((token) => {
            error.config.headers.Authorization = `Bearer ${token}`;
            error.config._retry = true;
            resolve(api(error.config));
          });
        });
      }

      isRefreshing = true;
      error.config._retry = true;

      try {
        // Use the centralized performTokenRefresh function
        const newAccessToken = await performTokenRefresh();

        if (newAccessToken) {
          console.log("✅ Token refreshed, retrying original request...");
          api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
          onRefreshed(newAccessToken);

          error.config.headers.Authorization = `Bearer ${newAccessToken}`;
          return api(error.config);
        } else {
          throw new Error("Token refresh returned no token");
        }
      } catch (refreshError) {
        console.error("❌ Reactive refresh failed:", refreshError);
        isRefreshing = false;
        error.config._skipLoginRedirect = true;
        return Promise.reject(error);
      } finally {
        isRefreshing = false;
      }
    }

    /* ✅ FIX #1: Updated condition - skip if refresh already handled or in progress
       🛑 1. SESSION EXPIRED ERROR (401 / 403) - Only if not a retry
    ----------------------------------------------------------------*/
    if (
      (status === 401 || status === 403) &&
      !error.config?._retry &&
      !error.config?._skipLoginRedirect
    ) {
      // Only redirect once per session
      if (!redirectInProgress) {
        redirectInProgress = true;
        toastRef.current?.error("Session expired. Redirecting to login...");

        setTimeout(() => {
          localStorage.removeItem("accessToken");
          localStorage.removeItem("refreshToken");
          localStorage.removeItem("userId");
          localStorage.removeItem("role");
          storeRef?.dispatch(logout());
          window.location.href = "/login";
        }, 2000);
      }

      return Promise.reject(error);
    }

    /* --------------------------------------------------------------
     🟡 2. NORMAL API FAILURE ERROR
    ----------------------------------------------------------------*/
    if (!skipToast) {
      toastRef.current?.error(message);
    }

    return Promise.reject(error.response?.data || error);
  },
);

export default api;

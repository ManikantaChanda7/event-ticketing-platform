// utils/api.js
import axios from "axios";
import { logout } from "../redux/slices/authSlice";
import { toastRef } from "../components/toastProvider";

let storeRef = null; // will be set from store setup

export const setStore = (store) => {
  storeRef = store;
};

// Refresh token queue/mutex to prevent multiple concurrent refresh calls
let isRefreshing = false;
let refreshSubscribers = [];

function addRefreshSubscriber(callback) {
  refreshSubscribers.push(callback);
}

function onRefreshed(token) {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
}

const api = axios.create({
  baseURL: "http://localhost:8080/api",
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

    if (
      status === 401 &&
      !error.config?._retry &&
      localStorage.getItem("refreshToken")
    ) {
      console.log("Token expired, attempting refresh...");
      if (isRefreshing) {
        console.log("Refresh already in progress, waiting...");
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
        const refreshResponse = await axios.post(
          "http://localhost:8080/api/auth/refresh",
          {
            refreshToken: localStorage.getItem("refreshToken"),
          },
        );

        console.log("Refresh response:", refreshResponse.data);
        const newAccessToken = refreshResponse.data.data.accessToken;
        const newRefreshToken = refreshResponse.data.data.refreshToken;

        console.log("Refresh successful, updating tokens...");
        localStorage.setItem("accessToken", newAccessToken);
        localStorage.setItem("refreshToken", newRefreshToken);

        api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;

        onRefreshed(newAccessToken);
        isRefreshing = false;

        error.config.headers.Authorization = `Bearer ${newAccessToken}`;
        console.log("Retrying original request with new token...");
        return api(error.config);
      } catch (refreshError) {
        console.error("Refresh failed:", refreshError);
        isRefreshing = false;
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("userId");
        localStorage.removeItem("role");

        toastRef.current?.error("Session expired. Redirecting to login...");
        setTimeout(() => {
          storeRef?.dispatch(logout());
          window.location.href = "/login";
        }, 2000);

        return Promise.reject(error);
      }
    }

    /* --------------------------------------------------------------
     🛑 1. SESSION EXPIRED ERROR (401 / 403) - Only if not a retry
    ----------------------------------------------------------------*/
    if ((status === 401 || status === 403) && !error.config?._retry) {
      toastRef.current?.error("Session expired. Redirecting to login...");

      setTimeout(() => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("userId");
        localStorage.removeItem("role");
        storeRef?.dispatch(logout());
        window.location.href = "/login";
      }, 2000);

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

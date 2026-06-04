// utils/api.js
import axios from "axios";
import { logout } from "../redux/slices/authSlice";
import { toastRef } from "../components/toastProvider";

let storeRef = null; // will be set from store setup

export const setStore = (store) => {
  storeRef = store;
};

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

// Global 403 handler
// api.interceptors.response.use(
//   (response) => response,
//   (error) => {
//     if (error.response?.status === 403 || error.response?.status === 401) {
//       showToast({
//         type: "error",
//         message: `Session timeout. Redirecting to login`,
//       });
//       setTimeout(() => {
//         localStorage.removeItem("token");
//         storeRef?.dispatch(logout());
//         window.location.href = "/login";
//       }, 3000);
//     }
//     return Promise.reject(error);
//   }
// );

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const status = error.response?.status;
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      "Something went wrong";

    if (
      status === 401 &&
      !error.config?._retry &&
      localStorage.getItem("refreshToken")
    ) {
      error.config._retry = true;

      try {
        const refreshResponse = await axios.post(
          "http://localhost:8080/api/auth/refresh",
          {
            refreshToken: localStorage.getItem("refreshToken"),
          },
        );

        const newAccessToken = refreshResponse.data.data.accessToken;
        const newRefreshToken = refreshResponse.data.data.refreshToken;

        localStorage.setItem("accessToken", newAccessToken);
        localStorage.setItem("refreshToken", newRefreshToken);

        error.config.headers.Authorization = `Bearer ${newAccessToken}`;

        return axios(error.config);
      } catch (refreshError) {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
      }
    }

    /* --------------------------------------------------------------
     🛑 1. SESSION EXPIRED ERROR (401 / 403)
    ----------------------------------------------------------------*/
    if (status === 401 || status === 403) {
      toastRef.current?.error("Session expired. Redirecting to login...");

      setTimeout(() => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        storeRef?.dispatch(logout());
        window.location.href = "/login";
      }, 2000);

      return Promise.reject(error);
    }

    /* --------------------------------------------------------------
     🟡 2. NORMAL API FAILURE ERROR
    ----------------------------------------------------------------*/
    toastRef.current?.error(message);

    return Promise.reject(error);
  },
);

export default api;

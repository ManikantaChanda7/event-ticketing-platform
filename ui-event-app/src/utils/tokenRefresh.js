import {
  TOKEN_CONFIG,
  getRefreshDelay,
  getTimeUntilExpiry,
} from "../config/tokenConfig";
import axios from "axios";
import { logout } from "../redux/slices/authSlice";
import { toastRef } from "../components/toastProvider";
import { getApiBaseUrl } from "../config/apiConfig";

let storeRef = null;
let refreshScheduled = false;
let refreshTimeout = null;
let lastRefreshAttempt = 0;

/**
 * Set the Redux store reference for dispatching logout action
 * Call this from your app initialization
 */
export const setRefreshStore = (store) => {
  storeRef = store;
};

/**
 * Clear any scheduled token refresh
 * Call this on logout or when tokens are removed
 */
export const clearRefreshSchedule = () => {
  if (refreshTimeout) {
    clearTimeout(refreshTimeout);
    refreshTimeout = null;
    refreshScheduled = false;
    console.log("🔄 Token refresh schedule cleared");
  }
};

/**
 * Perform token refresh with rate limiting to prevent rapid-fire attempts
 * This is called by both proactive schedule and reactive error handler
 */
export const performTokenRefresh = async () => {
  try {
    const refreshToken = localStorage.getItem("refreshToken");

    if (!refreshToken) {
      console.error("❌ No refresh token available");
      redirectToLogin("Refresh token expired. Please log in again.");
      return null;
    }

    // Rate limiting: don't attempt refresh more than once per minute
    const now = Date.now();
    if (now - lastRefreshAttempt < TOKEN_CONFIG.MIN_REFRESH_INTERVAL) {
      console.warn(
        `⏳ Refresh attempted too soon. Wait ${Math.round((TOKEN_CONFIG.MIN_REFRESH_INTERVAL - (now - lastRefreshAttempt)) / 1000)}s`,
      );
      return null;
    }

    lastRefreshAttempt = now;

    console.log("🔄 Performing token refresh...");

    const response = await axios.post(
      `${getApiBaseUrl()}/auth/refresh`,
      { refreshToken },
      { _skipToast: true }, // Don't show toast for refresh requests
    );

    const { accessToken, refreshToken: newRefreshToken } = response.data.data;

    // Update tokens
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", newRefreshToken);

    console.log("✅ Token refreshed successfully");

    // Schedule next refresh
    scheduleTokenRefresh();

    return accessToken;
  } catch (error) {
    console.error("❌ Token refresh failed:", error);
    clearRefreshSchedule();
    redirectToLogin("Session expired. Please log in again.");
    return null;
  }
};

/**
 * Schedule proactive token refresh before expiration
 * This ensures tokens are refreshed automatically without user seeing errors
 */
export const scheduleTokenRefresh = () => {
  // Clear existing schedule
  clearRefreshSchedule();

  try {
    const accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
      console.warn("⚠️ No access token found, cannot schedule refresh");
      return;
    }

    const timeUntilExpiry = getTimeUntilExpiry(accessToken);

    if (timeUntilExpiry === null || timeUntilExpiry <= 0) {
      console.warn("⚠️ Access token already expired or invalid");
      return;
    }

    // Calculate when to refresh (e.g., at 80% of token lifetime)
    const refreshDelay = Math.max(
      getRefreshDelay(),
      TOKEN_CONFIG.MIN_REFRESH_INTERVAL,
    );

    console.log(
      `⏱️ Token refresh scheduled in ${Math.round(refreshDelay / 1000)}s (expires in ${Math.round(timeUntilExpiry / 1000)}s)`,
    );

    refreshTimeout = setTimeout(() => {
      console.log("⏰ Scheduled refresh time reached, refreshing token...");
      performTokenRefresh();
    }, refreshDelay);

    refreshScheduled = true;
  } catch (error) {
    console.error("Error scheduling token refresh:", error);
  }
};

/**
 * Initialize token refresh on app load
 * Call this from App.jsx useEffect
 */
export const initializeTokenRefresh = () => {
  const accessToken = localStorage.getItem("accessToken");
  const refreshToken = localStorage.getItem("refreshToken");

  if (accessToken && refreshToken) {
    console.log("🚀 Initializing proactive token refresh");
    scheduleTokenRefresh();
  }
};

/**
 * Redirect user to login page and dispatch logout
 */
const redirectToLogin = (message) => {
  console.log("Redirecting to login...");

  if (storeRef) {
    storeRef.dispatch(logout());
  }

  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("userId");
  localStorage.removeItem("role");

  if (message) {
    toastRef.current?.error(message);
  }

  // Use setTimeout to allow Redux dispatch to complete
  setTimeout(() => {
    window.location.href = "/login";
  }, 500);
};

/**
 * Get current refresh status (useful for debugging)
 */
export const getRefreshStatus = () => ({
  refreshScheduled,
  lastRefreshAttempt: new Date(lastRefreshAttempt).toLocaleTimeString(),
  hasAccessToken: !!localStorage.getItem("accessToken"),
  hasRefreshToken: !!localStorage.getItem("refreshToken"),
});

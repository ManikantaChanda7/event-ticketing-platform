// Token expiration configuration
// Adjust these values based on your security requirements

export const TOKEN_CONFIG = {
  // Access token lifetime: 15 minutes
  // Short-lived for better security, expired tokens won't be usable for long
  ACCESS_TOKEN_EXPIRY: 15 * 60 * 1000, // milliseconds

  // Refresh token lifetime: 7 days
  // Allows user to stay logged in without re-entering credentials
  REFRESH_TOKEN_EXPIRY: 7 * 24 * 60 * 60 * 1000, // milliseconds

  // Refresh access token when 80% of its lifetime has passed
  // Example: If token expires in 15 mins, refresh after 12 mins
  // This ensures seamless user experience without expired token errors
  REFRESH_THRESHOLD: 0.8,

  // Minimum interval between refresh attempts (prevent rapid-fire refreshes)
  MIN_REFRESH_INTERVAL: 60 * 1000, // 1 minute
};

/**
 * Calculate delay before refreshing token
 * Returns milliseconds until refresh should happen
 */
export const getRefreshDelay = () => {
  const TOKEN_LIFETIME = TOKEN_CONFIG.ACCESS_TOKEN_EXPIRY;
  return Math.floor(TOKEN_LIFETIME * TOKEN_CONFIG.REFRESH_THRESHOLD);
};

/**
 * Calculate when token will expire
 * @param {string} token - JWT token
 * @returns {number} Expiration time in milliseconds since epoch
 */
export const getTokenExpiryTime = (token) => {
  try {
    // Simple JWT decode without external library
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    );
    const decoded = JSON.parse(jsonPayload);
    return decoded.exp * 1000; // Convert to milliseconds
  } catch (error) {
    console.error("Error decoding token:", error);
    return null;
  }
};

/**
 * Check if token is expired
 * @param {string} token - JWT token
 * @returns {boolean} True if token is expired
 */
export const isTokenExpired = (token) => {
  const expiryTime = getTokenExpiryTime(token);
  if (!expiryTime) return true;
  return Date.now() >= expiryTime;
};

/**
 * Get remaining time until token expires
 * @param {string} token - JWT token
 * @returns {number} Milliseconds until expiration, or null if invalid
 */
export const getTimeUntilExpiry = (token) => {
  const expiryTime = getTokenExpiryTime(token);
  if (!expiryTime) return null;
  const timeRemaining = expiryTime - Date.now();
  return timeRemaining > 0 ? timeRemaining : 0;
};

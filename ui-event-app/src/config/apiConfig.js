// Determine API base URL based on environment
const getApiBaseUrl = () => {
  // If explicitly set via environment variable
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL;
  }

  // Development - localhost
  if (
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1"
  ) {
    return "http://localhost:8080/api";
  }

  // Production - Vercel deployment
  if (window.location.hostname.includes("vercel.app")) {
    return "https://event-ticketing-platform-2mmm.onrender.com/api";
  }

  // Fallback: Try to use same protocol and host
  return `${window.location.protocol}//${window.location.host}/api`;
};

const getWebSocketUrl = () => {
  // If explicitly set via environment variable
  if (import.meta.env.VITE_WS_URL) {
    return import.meta.env.VITE_WS_URL;
  }

  const hostname = window.location.hostname;

  // Development - localhost
  if (hostname === "localhost" || hostname === "127.0.0.1") {
    return "http://localhost:8080/ws";
  }

  // Production - Vercel deployment (SockJS requires HTTP/HTTPS URL, not WS/WSS)
  if (hostname.includes("vercel.app")) {
    return "https://event-ticketing-platform-2mmm.onrender.com/ws";
  }

  // Fallback: Use same host as app with HTTP/HTTPS protocol (SockJS handles WebSocket upgrade)
  const protocol = window.location.protocol === "https:" ? "https" : "http";
  const port = window.location.port ? `:${window.location.port}` : "";
  return `${protocol}://${hostname}${port}/ws`;
};

export { getApiBaseUrl, getWebSocketUrl };

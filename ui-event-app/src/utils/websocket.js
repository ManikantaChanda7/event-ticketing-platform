import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { getWebSocketUrl } from "../config/apiConfig";

let stompClient = null;
let connected = false;
let subscriptions = [];
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 3000; // 3 seconds

export const connectWebSocket = (onMessageCallback) => {
  const wsUrl = getWebSocketUrl();
  console.log("Attempting WebSocket connection to:", wsUrl);

  const socket = new SockJS(wsUrl);
  stompClient = Stomp.over(socket);

  // Disable debug logging in production to avoid console spam
  stompClient.debug = false;

  stompClient.connect(
    {},
    () => {
      console.log("✅ WebSocket connected successfully");
      connected = true;
      reconnectAttempts = 0; // Reset attempts on successful connection

      // Subscribe to seat updates
      const subscription = stompClient.subscribe("/topic/seats", (message) => {
        try {
          const seatUpdate = JSON.parse(message.body);
          if (onMessageCallback) {
            onMessageCallback(seatUpdate);
          }
        } catch (error) {
          console.error("Error parsing seat update message:", error);
        }
      });
      subscriptions.push(subscription);
    },
    (error) => {
      console.error("❌ WebSocket connection error:", error);
      connected = false;

      // Implement exponential backoff retry
      if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++;
        const delay = RECONNECT_DELAY * Math.pow(1.5, reconnectAttempts - 1);
        console.log(
          `Reconnecting WebSocket in ${Math.round(delay / 1000)}s (Attempt ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})...`,
        );

        setTimeout(() => {
          if (!connected) {
            connectWebSocket(onMessageCallback);
          }
        }, delay);
      } else {
        console.error("❌ Max WebSocket reconnection attempts reached");
      }
    },
  );
};

export const disconnectWebSocket = () => {
  if (stompClient && connected) {
    subscriptions.forEach((sub) => sub.unsubscribe());
    subscriptions = [];
    stompClient.disconnect(() => {
      console.log("WebSocket disconnected");
      connected = false;
      reconnectAttempts = 0;
    });
  }
};

export const isConnected = () => connected;

export const getConnectionStatus = () => ({
  connected,
  clientReady: stompClient !== null,
  subscriptionsCount: subscriptions.length,
  reconnectAttempts,
  wsUrl: getWebSocketUrl(),
});

export const sendSeatUpdate = (seatUpdate) => {
  if (stompClient && connected) {
    try {
      stompClient.send("/app/seat/update", {}, JSON.stringify(seatUpdate));
      return true;
    } catch (error) {
      console.error("Error sending seat update:", error);
      return false;
    }
  } else {
    console.warn("WebSocket not connected. Seat update not sent.");
    return false;
  }
};

export const reconnectWebSocket = (onMessageCallback) => {
  if (connected && stompClient) {
    console.log("WebSocket already connected");
    return;
  }

  console.log("Attempting manual WebSocket reconnection...");
  disconnectWebSocket();
  setTimeout(() => {
    connectWebSocket(onMessageCallback);
  }, 1000);
};

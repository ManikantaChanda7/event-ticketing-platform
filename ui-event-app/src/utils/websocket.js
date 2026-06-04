import SockJS from "sockjs-client";
import Stomp from "stompjs";

let stompClient = null;
let connected = false;
let subscriptions = [];

export const connectWebSocket = (onMessageCallback) => {
  const socket = new SockJS("http://localhost:8080/ws");
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    connected = true;
    console.log("WebSocket connected");

    // Subscribe to seat updates
    const subscription = stompClient.subscribe("/topic/seats", (message) => {
      const seatUpdate = JSON.parse(message.body);
      if (onMessageCallback) {
        onMessageCallback(seatUpdate);
      }
    });
    subscriptions.push(subscription);
  }, (error) => {
    console.error("WebSocket connection error:", error);
    connected = false;
  });
};

export const disconnectWebSocket = () => {
  if (stompClient && connected) {
    subscriptions.forEach(sub => sub.unsubscribe());
    subscriptions = [];
    stompClient.disconnect(() => {
      connected = false;
      console.log("WebSocket disconnected");
    });
  }
};

export const isConnected = () => connected;

export const sendSeatUpdate = (seatUpdate) => {
  if (stompClient && connected) {
    stompClient.send("/app/seat/update", {}, JSON.stringify(seatUpdate));
  }
};

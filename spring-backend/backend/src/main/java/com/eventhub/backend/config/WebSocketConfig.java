package com.eventhub.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ FIX #2: Use explicit allowed origins instead of wildcard pattern
        // This fixes WebSocket connection issues in production with HTTPS
        String[] allowedOrigins = {
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173",
                "https://rezervo-booking.vercel.app",
                "https://event-ticketing-platform-2mmm.onrender.com"
        };

        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS()
                .setInterceptors(); // Enable interceptors for authentication if needed

        // Also add native WebSocket endpoint (without SockJS fallback)
        registry.addEndpoint("/ws-native")
                .setAllowedOrigins(allowedOrigins);
    }
}

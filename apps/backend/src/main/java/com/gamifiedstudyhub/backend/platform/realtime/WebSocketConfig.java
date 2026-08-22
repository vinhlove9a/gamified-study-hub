package com.gamifiedstudyhub.backend.platform.realtime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket broker for realtime pushes (leaderboard ticks, notifications).
 * OFF by default — no endpoint is registered unless {@code app.websocket.enabled=true}.
 *
 * <p>When enabling, also permit the handshake path in {@code SecurityConfig}
 * (e.g. allow {@code /ws/**}) — it is otherwise denied by the authenticated {@code /**}
 * rule. Uses the simple in-memory broker; swap {@code enableSimpleBroker} for a
 * relay (RabbitMQ/ActiveMQ) if you scale WebSocket across instances.
 */
@Configuration
@EnableWebSocketMessageBroker
@ConditionalOnProperty(prefix = "app.websocket", name = "enabled", havingValue = "true")
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS();
    }
}

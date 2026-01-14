package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.repositories.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PlainWebSocketHandler plainWebSocketHandler;
    private final UserRepository userRepository;

    public WebSocketConfig(PlainWebSocketHandler plainWebSocketHandler, UserRepository userRepository) {
        this.plainWebSocketHandler = plainWebSocketHandler;
        this.userRepository = userRepository;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(plainWebSocketHandler, "/ws")
                .addInterceptors(new JWTHandshakeInterceptor(userRepository))
                .setAllowedOriginPatterns("*");
    }

}


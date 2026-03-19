package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.security.JWTTokenComponent;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PlainWebSocketHandler plainWebSocketHandler;
    private final JWTTokenComponent jwtComponent;
    private final UserRepository userRepository;

    private final String WS_PATH = "/api/ws";

    public WebSocketConfig(PlainWebSocketHandler plainWebSocketHandler, JWTTokenComponent jwtComponent, UserRepository userRepository) {
        this.plainWebSocketHandler = plainWebSocketHandler;
        this.jwtComponent = jwtComponent;
        this.userRepository = userRepository;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(this.plainWebSocketHandler, this.WS_PATH)
                .addInterceptors(new JWTHandshakeInterceptor(this.jwtComponent, this.userRepository))
                .setAllowedOriginPatterns("*");
    }

}


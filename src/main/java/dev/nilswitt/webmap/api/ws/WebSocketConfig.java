package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.security.JWTComponent;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PlainWebSocketHandler plainWebSocketHandler;
    private final JWTComponent jwtComponent;

    public WebSocketConfig(PlainWebSocketHandler plainWebSocketHandler, JWTComponent jwtComponent) {
        this.plainWebSocketHandler = plainWebSocketHandler;
        this.jwtComponent = jwtComponent;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(plainWebSocketHandler, "/api/ws")
                .addInterceptors(new JWTHandshakeInterceptor(jwtComponent))
                .setAllowedOriginPatterns("*");
    }

}


package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JWTHandshakeInterceptor implements HandshakeInterceptor {


    private final Logger log = LoggerFactory.getLogger(JWTHandshakeInterceptor.class);


    private UserRepository userRepository;
    JWTHandshakeInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request.getHeaders().containsHeader("Authorization")) {
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader;
                if (jwtToken.startsWith("Bearer ")) {
                    jwtToken = jwtToken.substring(7);
                }

                attributes.put("jwtToken", jwtToken);
                attributes.put("user", userRepository.findAll().getFirst());
                log.info("JWT Token received: {}", jwtToken);
                return true;
            } else {
                log.warn("Invalid Authorization header format");
                return false;
            }
        } else {
            log.warn("Missing Authorization header");
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {

    }
}

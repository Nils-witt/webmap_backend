package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.security.JWTComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JWTHandshakeInterceptor implements HandshakeInterceptor {


    private final Logger log = LogManager.getLogger(JWTHandshakeInterceptor.class);


    private final JWTComponent jwtComponent;

    JWTHandshakeInterceptor(JWTComponent jwtComponent) {
        this.jwtComponent = jwtComponent;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request.getHeaders().containsHeader("Authorization")) {
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String jwtToken = authHeader;
                    if (jwtToken.startsWith("Bearer ")) {
                        jwtToken = jwtToken.substring(7);
                    }
                    User user = jwtComponent.getUserFromToken(jwtToken); // Validate token

                    attributes.put("jwtToken", jwtToken);
                    attributes.put("user", user);
                    return true;
                } catch (Exception e) {
                    log.warn("JWT validation failed: {}", e.getMessage());
                }
            } else {
                log.warn("Invalid Authorization header format");
            }
        } else {
            log.debug("Missing Authorization header");
        }
        if (request.getURI().getQuery() != null) {
            String[] queryParams = request.getURI().getQuery().split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals("token")) {
                    try {
                        User user = jwtComponent.getUserFromToken(keyValue[1]);

                        attributes.put("jwtToken", keyValue[1]);
                        attributes.put("user", user);
                        return true;
                    } catch (Exception e) {
                        log.warn("JWT validation failed: {}", e.getMessage());
                    }

                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {

    }
}

package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.security.JWTTokenComponent;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Log4j2
public class JWTHandshakeInterceptor implements HandshakeInterceptor {


    private final JWTTokenComponent jwtComponent;
    private final UserRepository userRepository;

    JWTHandshakeInterceptor(JWTTokenComponent jwtComponent, UserRepository userRepository) {
        this.jwtComponent = jwtComponent;
        this.userRepository = userRepository;
    }


    private User getUser(String token) {
        try {
            User user = jwtComponent.getUserFromToken(token); // Validate token
            return user;
        } catch (Exception e) {
            /* */
        }

        try {
            String username = jwtComponent.getUsernameFromSSOToken(token);
            return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            /* */

        }
        throw new RuntimeException("Invalid token");
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        if (request.getHeaders().containsHeader("Authorization")) {
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String jwtToken = authHeader;
                    if (jwtToken.startsWith("Bearer ")) {
                        jwtToken = jwtToken.substring(7);
                    }


                    attributes.put("jwtToken", jwtToken);
                    attributes.put("user", this.getUser(jwtToken)
                    );
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
                        attributes.put("jwtToken", keyValue[1]);
                        attributes.put("user", this.getUser(keyValue[1]));
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
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {

    }
}

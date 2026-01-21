package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.entities.SecurityGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class PlainWebSocketHandler extends AbstractWebSocketHandler {

    private static final String PING_PAYLOAD = "ping";
    private static final String PONG_PAYLOAD = "pong";

    private final WebSocketSessionRegistry sessionRegistry;

    private final Logger log = LoggerFactory.getLogger(PlainWebSocketHandler.class);

    public PlainWebSocketHandler(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }
    private final ArrayList<String> availableEntityTopics = new ArrayList<>(Arrays.stream(SecurityGroup.UserRoleTypeEnum.values()).map(r -> r.name().toLowerCase()).toList());

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.startsWith("SUBSCRIBE ")) {
            String topic = payload.substring(10).trim().toLowerCase();
            try {
                if (topic.startsWith("/updates/entities/")) {
                    String[] parts = topic.split("/");
                    if (parts.length >= 4) {
                        String entityType = parts[3];
                        if (!availableEntityTopics.contains(entityType)) {
                            throw new ForbiddenException("You do not have permission to subscribe to entity type: " + entityType);
                        }
                    } else {
                        throw new ForbiddenException("Invalid topic format: " + topic);
                    }
                }

                sessionRegistry.subscribe(session, topic);
                session.sendMessage(new TextMessage("Subscribed to " + topic));
            }catch (ForbiddenException e){
                session.sendMessage(new TextMessage("Subscription to topic " + topic + " denied: " + e.getMessage()));
                log.warn("Session {} denied subscription to topic {}: {}", session.getId(), topic, e.getMessage());
            }

            return;
        } else if (payload.equals(PING_PAYLOAD)) {
            session.sendMessage(new TextMessage(PONG_PAYLOAD));
            return;
        }
        session.sendMessage(new TextMessage("Unsupported message"));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionRegistry.add(session);
        log.info("WebSocket connection established: {} {}", session.getPrincipal(), session.getId());
        if (session.getAttributes().containsKey("user")) {
            log.info("WebSocket session user attribute: {}", session.getAttributes().get("user"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionRegistry.remove(session);
    }
}

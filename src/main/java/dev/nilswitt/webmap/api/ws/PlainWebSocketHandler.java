package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class PlainWebSocketHandler extends AbstractWebSocketHandler {

    private static final String PING_PAYLOAD = "ping";
    private static final String PONG_PAYLOAD = "pong";

    private final WebSocketSessionRegistry sessionRegistry;

    private final Logger log = LoggerFactory.getLogger(PlainWebSocketHandler.class);

    public PlainWebSocketHandler(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.startsWith("SUBSCRIBE ")) {
            String topic = payload.substring(10).trim();
            try {
                sessionRegistry.subscribe(session, topic);
                // Here you would add logic to register the subscription
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

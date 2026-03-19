package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.api.dtos.UnitDto;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
import dev.nilswitt.webmap.events.ChangeType;
import dev.nilswitt.webmap.exceptions.ForbiddenException;
import dev.nilswitt.webmap.security.PermissionVerifier;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PlainWebSocketHandler extends AbstractWebSocketHandler {

    private static final String PING_PAYLOAD = "ping";
    private static final String PONG_PAYLOAD = "pong";

    private final WebSocketSessionRegistry sessionRegistry;
    private final UnitRepository unitRepository;
    private final PermissionVerifier permissionsUtil;
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private final Logger log = LoggerFactory.getLogger(PlainWebSocketHandler.class);

    public PlainWebSocketHandler(WebSocketSessionRegistry sessionRegistry, UnitRepository unitRepository, PermissionVerifier permissionsUtil) {
        this.sessionRegistry = sessionRegistry;
        this.unitRepository = unitRepository;
        this.permissionsUtil = permissionsUtil;
    }

    private final ArrayList<String> availableEntityTopics = new ArrayList<>(Arrays.stream(SecurityGroup.UserRoleTypeEnum.values()).map(r -> r.name().toLowerCase()).toList());

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.startsWith("SUBSCRIBE ")) {
            String topic = payload.substring(10).trim().toLowerCase();
            try {
                if (topic.startsWith("/entities/")) {
                    String[] parts = topic.split("/");
                    if (parts.length >= 3) {
                        String entityType = parts[2];
                        if (!availableEntityTopics.contains(entityType)) {
                            throw new ForbiddenException("You do not have permission to subscribe to entity type: " + entityType);
                        }
                    } else {
                        throw new ForbiddenException("Invalid topic format: " + topic);
                    }
                }

                sessionRegistry.subscribe(session, topic);
                session.sendMessage(new TextMessage("Subscribed to " + topic));
            } catch (ForbiddenException e) {
                session.sendMessage(new TextMessage("Subscription to topic " + topic + " denied: " + e.getMessage()));
                log.warn("Session {} denied subscription to topic {}: {}", session.getId(), topic, e.getMessage());
            }

            return;
        } else if (payload.startsWith("UNSUBSCRIBE ")) {
            String topic = payload.substring(12).trim().toLowerCase();
            sessionRegistry.unsubscribe(session, topic);
            session.sendMessage(new TextMessage("Unsubscribed from " + topic));
        } else if (payload.startsWith("GET ")) {
            String topic = payload.substring(4).trim().toLowerCase();

            if (topic.equals("/entities/units")) {
                List<Unit> units = unitRepository.findAll();
                for (Unit unit : units) {
                    EntityUpdateNotifier.DownstreamMessage downstreamMessage = new EntityUpdateNotifier.DownstreamMessage();
                    downstreamMessage.topic = topic;

                    UnitDto dto = unit.toDto();
                    User userObj = (User) session.getAttributes().get("user");
                    dto.setPermissions(this.permissionsUtil.getScopes(unit, userObj));


                    downstreamMessage.payload = EntityUpdateNotifier.EntityUpdatedPayload.builder()
                            .entity(dto)
                            .changeType(ChangeType.RETRANSMIT)
                            .entityType(unit.getClass().getSimpleName())
                            .entityId(unit.getId())
                            .build();

                    session.sendMessage(new TextMessage(ow.writeValueAsString(downstreamMessage)));
                }
            } else {
                session.sendMessage(new TextMessage("Unknown topic: " + topic));
            }
            log.info("Session {} requested data for topic {}", session.getId(), topic);
        } else if (payload.equals(PING_PAYLOAD)) {
            session.sendMessage(new TextMessage(PONG_PAYLOAD));
            return;
        }
        session.sendMessage(new TextMessage("Unsupported message"));
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessionRegistry.add(session);
        log.info("WebSocket connection established: {} {}", session.getPrincipal(), session.getId());
        if (session.getAttributes().containsKey("user")) {
            log.info("WebSocket session user attribute: {}", session.getAttributes().get("user"));
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        sessionRegistry.remove(session);
    }
}

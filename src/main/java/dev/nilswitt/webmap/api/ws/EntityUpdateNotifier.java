package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.*;
import dev.nilswitt.webmap.events.ChangeType;
import dev.nilswitt.webmap.events.EntityChangedEvent;
import dev.nilswitt.webmap.security.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.util.*;

@Component
public class EntityUpdateNotifier {

    private static final Logger log = LoggerFactory.getLogger(EntityUpdateNotifier.class);

    private final WebSocketSessionRegistry registry;
    private final PermissionUtil permissionUtil;

    public EntityUpdateNotifier(WebSocketSessionRegistry registry, PermissionUtil permissionUtil) {
        this.registry = registry;
        this.permissionUtil = permissionUtil;
    }

    @EventListener
    @Async
    public void onUserNameChanged(EntityChangedEvent<? extends AbstractEntity> event) {

        Payload payload = buildPayload(event);
        String baseTopic = "/updates/entities/" + event.className().toLowerCase();
        String entityTopic = baseTopic + "/" + event.id();
        log.info("Updating entity {} to {}", entityTopic, baseTopic);

        List<String> topics = List.of(baseTopic, entityTopic);

        DownstreamMessage message = new DownstreamMessage();
        message.topic = entityTopic;
        message.payload = payload;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(message);

        HashMap<User, Set<String>> userSessions = new HashMap<>();

        for (String topic : topics) {
            for (WebSocketSession session : registry.getSessionsForTopic(topic)) {
                Object userObj = session.getAttributes().get("user");
                if (userObj instanceof User user) {
                    userSessions.computeIfAbsent(user, k -> new HashSet<>()).add(session.getId());
                }
            }
        }

        for (User user : userSessions.keySet()) {
            if (this.hasPermission(user, event)) {
                for (String sessionId : userSessions.get(user)) {
                    WebSocketSession session = registry.getSessionById(sessionId);
                    if (session != null && session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(json));
                        } catch (Exception e) {
                            log.error("Failed to send WebSocket message to session {}: {}", sessionId, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private boolean hasPermission(User user, EntityChangedEvent<? extends AbstractEntity> event) {
        AbstractEntity entity = event.entity();
        return switch (entity) {
            case User user1 -> permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, user1);
            case MapBaseLayer mapBaseLayer ->
                    permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, mapBaseLayer);
            case MapItem mapItem -> permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, mapItem);
            case MapOverlay mapOverlay ->
                    permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, mapOverlay);
            case Unit unit -> permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, unit);
            default -> false;
        };
    }

    private Payload buildPayload(EntityChangedEvent<? extends AbstractEntity> event) {
        Payload payload = new Payload();
        payload.entityType = event.entity().getClass().getSimpleName();
        payload.entityId = event.id();
        payload.changeType = event.changeType();
        payload.entity = event.entity();
        return payload;

    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }


    static class Payload {
        public String entityType;
        public UUID entityId;
        public ChangeType changeType;
        public AbstractEntity entity;
    }


    static class DownstreamMessage {
        public String topic;
        public Payload payload;
    }
}

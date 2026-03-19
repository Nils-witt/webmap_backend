package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.api.dtos.AbstractEntityDto;
import dev.nilswitt.webmap.entities.*;
import dev.nilswitt.webmap.events.ChangeType;
import dev.nilswitt.webmap.events.EntityChangedEvent;
import dev.nilswitt.webmap.security.PermissionVerifier;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
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


/**
 * This component listens for EntityChangedEvent events and notifies all connected WebSocket clients that have subscribed to the relevant topics about the changes.
 * It checks the permissions of each user before sending the update to ensure that only authorized users receive the information.
 */
@Component
public class EntityUpdateNotifier {

    private static final Logger log = LoggerFactory.getLogger(EntityUpdateNotifier.class);

    private final WebSocketSessionRegistry registry;
    private final PermissionVerifier permissionVerifier;
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private final String baseTopic = "entities";

    public EntityUpdateNotifier(WebSocketSessionRegistry registry, PermissionVerifier permissionVerifier) {
        this.registry = registry;
        this.permissionVerifier = permissionVerifier;
    }

    @EventListener
    @Async
    public void onUserNameChanged(EntityChangedEvent<? extends AbstractEntity> event) {

        String entityTypeName = event.className().toLowerCase();

        // Notify the type subscribers and individual entity subscribers
        List<String> topics = List.of(
                StringUtils.join(List.of("", this.baseTopic, entityTypeName), "/"),
                StringUtils.join(List.of("", this.baseTopic, entityTypeName, event.id()), "/")
        );


        // Find the sessions that are subscribed to one of topics
        HashMap<User, Set<String>> userSessions = new HashMap<>();

        for (String topic : topics) {
            log.info("Sending Update to " + topic);
            for (WebSocketSession session : registry.getSessionsForTopic(topic)) {
                Object userObj = session.getAttributes().get("user");

                if (userObj instanceof User user) {
                    log.info("Checking session for " + session.getId() + " user " + user.getId());

                    userSessions.computeIfAbsent(user, k -> new HashSet<>()).add(session.getId());
                }
            }
        }

        // For each session check of permissions and then send the message
        for (User user : userSessions.keySet()) {
            if (this.hasPermission(user, event)) {
                for (String sessionId : userSessions.get(user)) {
                    WebSocketSession session = registry.getSessionById(sessionId);
                    if (session != null && session.isOpen()) {
                        try {
                            session.sendMessage(buildMessage(event, user));
                        } catch (Exception e) {
                            log.error("Failed to send WebSocket message to session {}: {}", sessionId, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private TextMessage buildMessage(EntityChangedEvent<? extends AbstractEntity> event, User user) {
        DownstreamMessage message = new DownstreamMessage();
        message.topic = StringUtils.join(List.of("", this.baseTopic, event.className().toLowerCase(), event.id()), "/");
        message.payload = buildPayload(event, user);
        String json = ow.writeValueAsString(message);
        return new TextMessage(json);
    }

    private boolean hasPermission(User user, EntityChangedEvent<? extends AbstractEntity> event) {
        AbstractEntity entity = event.entity();
        return switch (entity) {
            case User user1 -> permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, user1);
            case MapBaseLayer mapBaseLayer ->
                    permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, mapBaseLayer);
            case MapItem mapItem -> permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, mapItem);
            case MapOverlay mapOverlay ->
                    permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, mapOverlay);
            case Unit unit -> permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.VIEW, unit);
            default -> false;
        };
    }

    private EntityUpdatedPayload buildPayload(EntityChangedEvent<? extends AbstractEntity> event, User user) {

        AbstractEntityDto dto = event.entity().toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(event.entity(), user));


        return EntityUpdatedPayload.builder()
                .entity(dto)
                .entityType(event.entity().getClass().getSimpleName())
                .changeType(event.changeType())
                .entityId(event.id())
                .build();
    }

    @Builder
    static class EntityUpdatedPayload {
        public String entityType;
        public UUID entityId;
        public ChangeType changeType;
        public AbstractEntityDto entity;
    }


    static class DownstreamMessage {
        public String topic;
        public EntityUpdatedPayload payload;
    }
}

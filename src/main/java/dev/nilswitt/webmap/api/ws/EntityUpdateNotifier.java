package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.AbstractEntity;
import dev.nilswitt.webmap.events.ChangeType;
import dev.nilswitt.webmap.events.EntityChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.util.UUID;

@Component
public class EntityUpdateNotifier {

    private static final Logger log = LoggerFactory.getLogger(EntityUpdateNotifier.class);

    private final WebSocketSessionRegistry registry;

    public EntityUpdateNotifier(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @EventListener
    public void onUserNameChanged(EntityChangedEvent<? extends AbstractEntity> event) {

        Payload payload = buildPayload(event);
        String topic = "/updates/entities/" + event.className() + "/" + event.id();

        DownstreamMessage message = new DownstreamMessage();
        message.topic = topic;
        message.payload = payload;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(message);
        log.info("Sending update to topic {}", topic);

        registry.notifyTopic(topic, json);

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

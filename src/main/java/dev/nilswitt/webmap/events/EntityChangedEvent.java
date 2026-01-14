package dev.nilswitt.webmap.events;

import dev.nilswitt.webmap.entities.AbstractEntity;

import java.util.Map;
import java.util.UUID;

public record EntityChangedEvent<T extends AbstractEntity>(Class<T> clazz, T entity, ChangeType changeType, UUID id, Map<String, Object> changes) {
}

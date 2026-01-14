package dev.nilswitt.webmap.events;

import dev.nilswitt.webmap.entities.AbstractEntity;

import java.util.UUID;

public record EntityChangedEvent<T extends AbstractEntity>(String className, T entity, ChangeType changeType, UUID id) {
}

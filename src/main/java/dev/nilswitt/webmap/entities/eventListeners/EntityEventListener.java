package dev.nilswitt.webmap.entities.eventListeners;

import dev.nilswitt.webmap.entities.AbstractEntity;
import dev.nilswitt.webmap.events.ChangeType;
import dev.nilswitt.webmap.events.EntityChangedEvent;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This Component is triggered by Hibernate Change Events and is publishing them as EntityChangedEvent to the Spring Application Context.
 * This allows us to react to changes in our entities in a decoupled way, for example by notifying WebSocket clients about updates.
 */
@Slf4j
public class EntityEventListener {
    private ApplicationEventPublisher applicationEventPublisher;

    public EntityEventListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostPersist
    public void onPostPersist(AbstractEntity entity) {
        log.debug("A new {} entity has been persisted: {}", entity.getClass().getSimpleName(), entity);
        applicationEventPublisher.publishEvent(new EntityChangedEvent<>(entity.getClass().getSimpleName(), entity, ChangeType.CREATED, entity.getId()));
    }

    @PostUpdate
    public void onPostUpdate(AbstractEntity entity) {
        log.debug("An existing {} entity has been updated: {}", entity.getClass().getSimpleName(), entity);
        applicationEventPublisher.publishEvent(new EntityChangedEvent<>(entity.getClass().getSimpleName(), entity, ChangeType.UPDATED, entity.getId()));

    }

    @PostRemove
    public void onPostRemove(AbstractEntity entity) {
        log.debug("An existing {} entity has been removed: {}", entity.getClass().getSimpleName(), entity);
        applicationEventPublisher.publishEvent(new EntityChangedEvent<>(entity.getClass().getSimpleName(), entity, ChangeType.DELETED, entity.getId()));
    }
}

package dev.nilswitt.webmap.entities.eventListeners;

import dev.nilswitt.webmap.entities.AbstractEntity;
import dev.nilswitt.webmap.events.ChangeType;
import dev.nilswitt.webmap.events.EntityChangedEvent;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

public class EntityEventListener {
    protected Logger logger = LoggerFactory.getLogger(EntityEventListener.class);

    private ApplicationEventPublisher applicationEventPublisher;

    public EntityEventListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostPersist
    public void onPostPersist(AbstractEntity entity) {
        logger.debug("A new {} entity has been persisted: {}", entity.getClass().getSimpleName(), entity);
        applicationEventPublisher.publishEvent(new EntityChangedEvent<>(entity.getClass().getSimpleName(), entity, ChangeType.CREATED, entity.getId()));
    }

    @PostUpdate
    public void onPostUpdate(AbstractEntity entity) {
        logger.debug("An existing {} entity has been updated: {}", entity.getClass().getSimpleName(), entity);
        applicationEventPublisher.publishEvent(new EntityChangedEvent<>(entity.getClass().getSimpleName(), entity, ChangeType.UPDATED, entity.getId()));

    }

    @PostRemove
    public void onPostRemove(AbstractEntity entity) {
        logger.debug("An existing {} entity has been removed: {}", entity.getClass().getSimpleName(), entity);
        applicationEventPublisher.publishEvent(new EntityChangedEvent<>(entity.getClass().getSimpleName(), entity, ChangeType.DELETED, entity.getId()));
    }
}

package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.AbstractEntityDto;
import dev.nilswitt.webmap.entities.eventListeners.EntityEventListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(EntityEventListener.class)
@Getter
@Setter
public class UnitPositionLog extends AbstractEntity {

    @Embedded
    private EmbeddedPosition position;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Override
    public AbstractEntityDto toDto() {
        return new AbstractEntityDto();
    }
}

package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.entities.eventListeners.EntityEventListener;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(EntityEventListener.class)
public class MapItem extends AbstractEntity {

    @Getter
    @Setter
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = false, length = 100)
    private String name;


    @Setter
    @Embedded
    private EmbeddedPosition position;

    public EmbeddedPosition getPosition() {
        if (position == null) {
            position = new EmbeddedPosition();
        }
        return position;
    }


}
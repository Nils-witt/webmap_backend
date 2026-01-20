package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.entities.eventListeners.EntityEventListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(EntityEventListener.class)
@Getter
@Setter
public class Unit extends AbstractEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = false, length = 100)
    private String name;


    @Embedded
    private EmbeddedPosition position;

    @Column(nullable = false)
    private int status = 6;

    @Column(nullable = false)
    private boolean speakRequest = false;

    public EmbeddedPosition getPosition() {
        if (position == null) {
            position = new EmbeddedPosition();
        }
        return position;
    }

}
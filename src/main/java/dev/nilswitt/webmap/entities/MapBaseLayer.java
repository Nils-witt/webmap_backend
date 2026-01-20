package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.entities.eventListeners.EntityEventListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(EntityEventListener.class)
@Getter
@Setter
public class MapBaseLayer extends AbstractEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = false, length = 100)
    private String name;

    @Column(nullable = false, unique = false, length = 100)
    private String url;

}
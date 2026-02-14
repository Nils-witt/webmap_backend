package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.AbstractEntityDto;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;


@MappedSuperclass
@Getter
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;


    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }


    public abstract AbstractEntityDto toDto();

}
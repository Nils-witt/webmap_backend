package dev.nilswitt.webmap.api.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class UnitPositionLogDto extends AbstractEntityDto {
    private UUID unitId;
    private EmbeddedPositionDto position;

    public UnitPositionLogDto(UUID id, Instant createdAt, Instant updatedAt, UUID unitId, EmbeddedPositionDto position) {
        super(id, createdAt, updatedAt);
        this.unitId = unitId;
        this.position = position;
    }
}

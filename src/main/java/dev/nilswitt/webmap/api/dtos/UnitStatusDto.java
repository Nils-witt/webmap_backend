package dev.nilswitt.webmap.api.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnitStatusDto extends AbstractEntityDto {

    private int status;
    private boolean acknowledged;
    private UUID unitId;


    public UnitStatusDto(UUID id, Instant createdAt, Instant updatedAt, int status, boolean acknowledged, UUID unitId) {
        super(id, createdAt, updatedAt);
        this.status = status;
        this.unitId = unitId;
        this.acknowledged = acknowledged;
    }
}

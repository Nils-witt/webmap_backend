package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.UnitPositionLogDto;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class UnitPositionLog extends AbstractEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;


    @Embedded
    private EmbeddedPosition position;

    public UnitPositionLog() {
    }


    public UnitPositionLog(Unit unit, EmbeddedPosition position) {
        this.unit = unit;
        this.position = position;
    }

    @Override
    public UnitPositionLogDto toDto() {
        return new UnitPositionLogDto(this.getId(), this.getCreatedAt(), this.getUpdatedAt(), this.unit.getId(), this.position.toDto());
    }


    @Override
    public String toString() {
        return "UnitPositionLog{" +
                "position=" + position +
                ", id=" + id +
                ", unit=" + unit +
                '}';
    }
}

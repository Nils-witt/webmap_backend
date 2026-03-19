package dev.nilswitt.webmap.entities;


import dev.nilswitt.webmap.api.dtos.UnitStatusDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
public class UnitStatusUpdate extends AbstractEntity {

    @Column(nullable = false)
    private int status = 6;

    @Column(nullable = false)
    private boolean acknowledged = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    public UnitStatusDto toDto() {

        return new UnitStatusDto(this.id, this.getCreatedAt(), this.getUpdatedAt(), this.status, this.acknowledged, this.unit.getId());
    }


    public UnitStatusUpdate() {

    }
}

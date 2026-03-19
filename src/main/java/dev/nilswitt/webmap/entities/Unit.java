package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.UnitDto;
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
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Embedded
    private TacticalIcon icon = new TacticalIcon();

    @Embedded
    private EmbeddedPosition position = new EmbeddedPosition();

    @Column(nullable = false)
    private int status = 6;

    @Column(nullable = false)
    private boolean speakRequest = false;

    @ManyToOne
    @JoinColumn(name = "mission_group_id")
    private MissionGroup missionGroup;

    public EmbeddedPosition getPosition() {
        if (position == null) {
            position = new EmbeddedPosition();
        }
        return position;
    }

    public UnitDto toDto() {
        return new UnitDto(
                this.getId(),
                this.getCreatedAt(),
                this.getUpdatedAt(),
                this.getName(),
                getIcon() != null ? this.getIcon().toDto() : null,
                this.getPosition() != null ? this.getPosition().toDto() : null,
                this.isSpeakRequest(),
                this.status
        );
    }

    public static Unit of(UnitDto dto) {
        Unit unit = new Unit();
        unit.setName(dto.getName());
        unit.setIcon(TacticalIcon.of(dto.getIcon()));
        unit.setSpeakRequest(dto.isSpeakRequest());
        unit.setStatus(dto.getStatus());
        if (dto.getPosition() != null) {
            unit.setPosition(EmbeddedPosition.of(dto.getPosition()));
        }
        return unit;
    }

}
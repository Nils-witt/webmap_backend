package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.EmbeddedPositionDto;
import dev.nilswitt.webmap.api.dtos.UnitDto;
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
@Getter
@Setter
public class Unit extends AbstractEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Embedded
    private TacticalIcon icon = new TacticalIcon();

    @Embedded
    private EmbeddedPosition position;

    @Column(nullable = false)
    private int status = 6;

    @Column(nullable = false)
    private boolean speakRequest = false;

    @Column(nullable = false)
    private boolean showOnMap = false;

    public EmbeddedPosition getPosition() {
        if (position == null) {
            position = new EmbeddedPosition();
        }
        return position;
    }
    public UnitDto toDto() {
        UnitDto dto = new UnitDto();
        dto.setId(getId());
        dto.setName(getName());
        dto.setStatus(getStatus());
        dto.setSpeakRequest(isSpeakRequest());
        dto.setShowOnMap(isShowOnMap());
        dto.setIcon(getIcon());
        EmbeddedPositionDto positionDto = new EmbeddedPositionDto();
        positionDto.setLatitude(getPosition().getLatitude());
        positionDto.setLongitude(getPosition().getLongitude());
        positionDto.setAltitude(getPosition().getAltitude());
        positionDto.setTimestamp(getPosition().getTimestamp());
        dto.setPosition(positionDto);
        return dto;
    }

    public static Unit of(UnitDto dto) {
        Unit unit = new Unit();
        unit.setName(dto.getName());
        unit.setStatus(dto.getStatus());
        unit.setSpeakRequest(dto.isSpeakRequest());
        unit.setShowOnMap(dto.isShowOnMap());
        unit.setIcon(dto.getIcon());
        if (dto.getPosition() != null) {
            EmbeddedPosition position = new EmbeddedPosition();
            position.setLatitude(dto.getPosition().getLatitude());
            position.setLongitude(dto.getPosition().getLongitude());
            position.setAltitude(dto.getPosition().getAltitude());
            position.setTimestamp(dto.getPosition().getTimestamp());
            unit.setPosition(position);
        }
        return unit;
    }

}
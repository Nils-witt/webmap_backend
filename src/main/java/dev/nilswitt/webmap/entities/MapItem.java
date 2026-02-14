package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.EmbeddedPositionDto;
import dev.nilswitt.webmap.api.dtos.MapItemDto;
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
public class MapItem extends AbstractEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = false, length = 100)
    private String name;

    @Embedded
    private EmbeddedPosition position;

    @ManyToOne
    @JoinColumn(name = "map_group_id")
    private MapGroup mapGroup;

    public EmbeddedPosition getPosition() {
        if (position == null) {
            position = new EmbeddedPosition();
        }
        return position;
    }

    public MapItemDto toDto() {
        MapItemDto dto = new MapItemDto();
        dto.setId(getId());
        dto.setName(getName());
        EmbeddedPositionDto positionDto = new EmbeddedPositionDto();
        positionDto.setLatitude(getPosition().getLatitude());
        positionDto.setLongitude(getPosition().getLongitude());
        positionDto.setAltitude(getPosition().getAltitude());
        positionDto.setTimestamp(getPosition().getTimestamp());
        dto.setPosition(positionDto);
        dto.setMapGroupId(getMapGroup() != null ? getMapGroup().getId() : null);
        return dto;
    }
}
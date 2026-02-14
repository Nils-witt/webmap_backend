package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.AbstractEntityDto;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Position extends AbstractEntity implements PositionInterface {
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double altitude = 0.0;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public AbstractEntityDto toDto() {
        AbstractEntityDto dto = new AbstractEntityDto();
        dto.setId(getId());
        return dto;
    }

}
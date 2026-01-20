package dev.nilswitt.webmap.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedPosition implements PositionInterface {
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double altitude = 0.0;

    public EmbeddedPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
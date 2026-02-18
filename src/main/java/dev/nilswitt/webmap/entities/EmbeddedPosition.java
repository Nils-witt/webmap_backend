package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.EmbeddedPositionDto;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedPosition implements PositionInterface {
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double altitude = 0.0;
    private double accuracy = 0.0;

    @Column
    private LocalDateTime timestamp = null;

    public EmbeddedPosition(double latitude, double longitude, double accuracy, LocalDateTime timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
    }

    public static EmbeddedPosition of(EmbeddedPositionDto entity) {
        return new EmbeddedPosition(entity.getLatitude(), entity.getLongitude(), entity.getAltitude(), entity.getAccuracy(), entity.getTimestamp());
    }

}
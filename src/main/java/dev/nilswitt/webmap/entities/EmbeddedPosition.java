package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.EmbeddedPositionDto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Position implementation used for all position properties in the application.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedPosition implements PositionInterface {

    @Column(name = "embeddedposition_latitude")
    private Double latitude = 0.0;

    @Column(name = "embeddedposition_longitude")
    private Double longitude = 0.0;

    @Column(name = "embeddedposition_altimeter")
    private Double altitude = 0.0;

    @Column(name = "embeddedposition_accuracy")
    private Double accuracy = 0.0;

    @Column(name = "embeddedposition_timestamp")
    private Instant timestamp = null;

    public EmbeddedPosition(Double latitude, Double longitude, Double accuracy, Instant timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
    }

    public static EmbeddedPosition of(EmbeddedPositionDto entity) {
        if (entity == null) {
            return null;
        }
        return new EmbeddedPosition(entity.getLatitude(), entity.getLongitude(), entity.getAltitude(), entity.getAccuracy(), entity.getTimestamp());
    }

    public EmbeddedPositionDto toDto() {
        EmbeddedPositionDto dto = new EmbeddedPositionDto();
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setAltitude(altitude);
        dto.setAccuracy(accuracy);
        dto.setTimestamp(timestamp);
        return dto;
    }

    @Override
    public String toString() {
        return "EmbeddedPosition{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", accuracy=" + accuracy +
                ", timestamp=" + timestamp +
                '}';
    }
}
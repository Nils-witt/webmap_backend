package dev.nilswitt.webmap.api.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmbeddedPositionDto {
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double altitude = 0.0;
    private LocalDateTime timestamp = null;
}

package dev.nilswitt.webmap.api.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class MapItemDto extends AbstractEntityDto {
    private String name;
    private EmbeddedPositionDto position;
    private UUID mapGroupId;
}

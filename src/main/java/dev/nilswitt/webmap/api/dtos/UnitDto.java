package dev.nilswitt.webmap.api.dtos;

import dev.nilswitt.webmap.entities.TacticalIcon;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnitDto extends AbstractEntityDto {

    private String name;
    private TacticalIcon icon = null;
    private EmbeddedPositionDto position;
    private int status = 6;
    private boolean speakRequest = false;
    private boolean showOnMap = false;
}

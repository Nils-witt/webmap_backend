package dev.nilswitt.webmap.api.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MapBaseLayerDto extends AbstractEntityDto {

    private String name;

    private String url;
}

package dev.nilswitt.webmap.api.rest.v1.map;

import dev.nilswitt.webmap.api.dtos.MapGroupDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class MapGroupModelAssembler implements RepresentationModelAssembler<MapGroupDto, EntityModel<MapGroupDto>> {

    @Override
    public EntityModel<MapGroupDto> toModel(MapGroupDto mapGroupDto) {

        return EntityModel.of(mapGroupDto,
                WebMvcLinkBuilder.linkTo(methodOn(MapGroupController.class).one(mapGroupDto.getId(), null)).withSelfRel(),
                linkTo(methodOn(MapGroupController.class).all(null)).withRel("map/groups"));
    }
}

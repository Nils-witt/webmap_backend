package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.api.dtos.MapItemDto;
import dev.nilswitt.webmap.entities.MapItem;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class MapItemModelAssembler implements RepresentationModelAssembler<MapItemDto, EntityModel<MapItemDto>> {

    @Override
    public EntityModel<MapItemDto> toModel(MapItemDto mapItem) {

        return EntityModel.of(mapItem,
                WebMvcLinkBuilder.linkTo(methodOn(MapItemController.class).one(mapItem.getId(), null)).withSelfRel(),
                linkTo(methodOn(MapItemController.class).all(null)).withRel("map/items"));
    }
}

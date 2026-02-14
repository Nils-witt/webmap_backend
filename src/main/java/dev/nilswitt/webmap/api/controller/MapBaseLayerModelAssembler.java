package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.api.dtos.MapBaseLayerDto;
import dev.nilswitt.webmap.entities.MapBaseLayer;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class MapBaseLayerModelAssembler implements RepresentationModelAssembler<MapBaseLayerDto, EntityModel<MapBaseLayerDto>> {

    @Override
    public EntityModel<MapBaseLayerDto> toModel(MapBaseLayerDto entity) {

        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(MapBaseLayerController.class).one(entity.getId(), null)).withSelfRel(),
                linkTo(methodOn(MapBaseLayerController.class).all(null)).withRel("map/baselayers"));
    }
}

package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.entities.MapBaseLayer;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class MapBaseLayerModelAssembler implements RepresentationModelAssembler<MapBaseLayer, EntityModel<MapBaseLayer>> {

    @Override
    public EntityModel<MapBaseLayer> toModel(MapBaseLayer entity) {

        return EntityModel.of(entity,
                linkTo(methodOn(MapBaseLayerController.class).one(entity.getId())).withSelfRel(),
                linkTo(methodOn(MapBaseLayerController.class).all()).withRel("map/baselayers"));
    }
}

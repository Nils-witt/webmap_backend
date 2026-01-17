package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.entities.MapOverlay;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class MapOverlayModelAssembler implements RepresentationModelAssembler<MapOverlay, EntityModel<MapOverlay>> {

    @Override
    public EntityModel<MapOverlay> toModel(MapOverlay mapItem) {

        return EntityModel.of(mapItem,
                linkTo(methodOn(MapOverlayController.class).one(mapItem.getId())).withSelfRel(),
                linkTo(methodOn(MapOverlayController.class).all()).withRel("map/items"));
    }
}

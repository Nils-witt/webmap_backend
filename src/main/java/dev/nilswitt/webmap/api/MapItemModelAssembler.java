package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.entities.MapItem;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class MapItemModelAssembler implements RepresentationModelAssembler<MapItem, EntityModel<MapItem>> {

    @Override
    public EntityModel<MapItem> toModel(MapItem mapItem) {

        return EntityModel.of(mapItem,
                linkTo(methodOn(MapItemController.class).one(mapItem.getId(), null)).withSelfRel(),
                linkTo(methodOn(MapItemController.class).all(null)).withRel("map/items"));
    }
}

package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.entities.Unit;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api/map")
public class MapController {

    @GetMapping("")
    CollectionModel<EntityModel<Unit>> all() {

        return CollectionModel.of(new ArrayList<>(),
                linkTo(methodOn(MapBaseLayerController.class).all(null)).withRel("baselayers"),
                linkTo(methodOn(MapOverlayController.class).all(null)).withRel("overlays"),
                linkTo(methodOn(MapItemController.class).all(null)).withRel("items")
        );
    }
}

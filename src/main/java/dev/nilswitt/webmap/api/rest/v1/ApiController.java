package dev.nilswitt.webmap.api.rest.v1;

import dev.nilswitt.webmap.api.rest.v1.map.MapController;
import dev.nilswitt.webmap.entities.Unit;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api")
public class ApiController {

    @GetMapping("")
    CollectionModel<EntityModel<Unit>> all() {

        return CollectionModel.of(new ArrayList<>(),
                WebMvcLinkBuilder.linkTo(methodOn(UserController.class).all(null)).withRel("users"),
                WebMvcLinkBuilder.linkTo(methodOn(UnitController.class).all(null)).withRel("units"),
                WebMvcLinkBuilder.linkTo(methodOn(MapController.class).all(null)).withRel("map"),
                WebMvcLinkBuilder.linkTo(methodOn(MissionGroupController.class).all(null)).withRel("missionGroups")
        );
    }

}

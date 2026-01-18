package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.entities.Unit;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class UnitModelAssembler implements RepresentationModelAssembler<Unit, EntityModel<Unit>> {

    @Override
    public EntityModel<Unit> toModel(Unit unit) {

        return EntityModel.of(unit,
                linkTo(methodOn(UnitController.class).one(unit.getId(), null)).withSelfRel(),
                linkTo(methodOn(UnitController.class).all(null)).withRel("units"));
    }
}

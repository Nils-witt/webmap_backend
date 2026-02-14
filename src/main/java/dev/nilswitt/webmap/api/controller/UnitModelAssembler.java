package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.api.dtos.UnitDto;
import dev.nilswitt.webmap.entities.Unit;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class UnitModelAssembler implements RepresentationModelAssembler<UnitDto, EntityModel<UnitDto>> {

    @Override
    public EntityModel<UnitDto> toModel(UnitDto unit) {

        return EntityModel.of(unit,
                WebMvcLinkBuilder.linkTo(methodOn(UnitController.class).one(unit.getId(), null)).withSelfRel(),
                linkTo(methodOn(UnitController.class).all(null)).withRel("units"));
    }
}

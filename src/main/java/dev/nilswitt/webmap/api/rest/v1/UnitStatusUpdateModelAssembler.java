package dev.nilswitt.webmap.api.rest.v1;

import dev.nilswitt.webmap.api.dtos.UnitStatusDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class UnitStatusUpdateModelAssembler implements RepresentationModelAssembler<UnitStatusDto, EntityModel<UnitStatusDto>> {

    @Override
    public EntityModel<UnitStatusDto> toModel(UnitStatusDto unit) {

        return EntityModel.of(unit,
                WebMvcLinkBuilder.linkTo(methodOn(UnitController.class).one(unit.getId(), null)).withSelfRel(),
                linkTo(methodOn(UnitController.class).all(null)).withRel("units"));
    }
}

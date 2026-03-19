package dev.nilswitt.webmap.api.rest.v1;

import dev.nilswitt.webmap.api.dtos.UnitPositionLogDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class UnitPositionLogModelAssembler implements RepresentationModelAssembler<UnitPositionLogDto, EntityModel<UnitPositionLogDto>> {

    @Override
    public EntityModel<UnitPositionLogDto> toModel(UnitPositionLogDto unit) {

        return EntityModel.of(unit,
                WebMvcLinkBuilder.linkTo(methodOn(UnitController.class).one(unit.getId(), null)).withSelfRel(),
                linkTo(methodOn(UnitController.class).all(null)).withRel("units"));
    }
}

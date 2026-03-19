package dev.nilswitt.webmap.api.rest.v1;

import dev.nilswitt.webmap.api.dtos.MissionGroupDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class MissionGroupModelAssembler implements RepresentationModelAssembler<MissionGroupDto, EntityModel<MissionGroupDto>> {

    @Override
    public EntityModel<MissionGroupDto> toModel(MissionGroupDto missionDto) {

        return EntityModel.of(missionDto,
                WebMvcLinkBuilder.linkTo(methodOn(MissionGroupController.class).one(missionDto.getId(), null)).withSelfRel());
    }
}

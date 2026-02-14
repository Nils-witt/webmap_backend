package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.api.dtos.UserDto;
import dev.nilswitt.webmap.entities.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserDto, EntityModel<UserDto>> {

    @Override
    public EntityModel<UserDto> toModel(UserDto user) {

        return EntityModel.of(user,
                WebMvcLinkBuilder.linkTo(methodOn(UserController.class).one(user.getId(), null)).withSelfRel(),
                linkTo(methodOn(UserController.class).all(null)).withRel("users"));
    }
}

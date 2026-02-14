package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.api.dtos.UserDto;
import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.UserNotFoundException;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.security.PermissionUtil;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserRepository repository;
    private final UserModelAssembler assembler;
    private final PermissionUtil permissionUtil;

    public UserController(UserRepository repository, UserModelAssembler assembler, PermissionUtil permissionUtil) {
        this.repository = repository;
        this.assembler = assembler;
        this.permissionUtil = permissionUtil;
    }

    @GetMapping("")
    public CollectionModel<EntityModel<UserDto>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.USER)) {

            List<EntityModel<UserDto>> entities = this.repository.findAll().stream()
                    .map(User::toDto)
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(UserController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionUtil.getUsersForUser(userDetails).stream().map(User::toDto).map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(UserController.class).all(null)).withSelfRel());

    }

    @PostMapping("")
    EntityModel<UserDto> newEmployee(@RequestBody UserDto newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.USER)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        User newUser = User.of(newEntity);



        return this.assembler.toModel(this.repository.save(newUser).toDto());
    }

    @GetMapping("{id}")
    public EntityModel<UserDto> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        User entity = this.repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        return this.assembler.toModel(entity.toDto());
    }

    @PutMapping("{id}")
    EntityModel<UserDto> replaceEntity(@RequestBody UserDto newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        User entity = this.repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setUsername(newEntity.getUsername());
        entity.setEmail(newEntity.getEmail());
        entity.setFirstName(newEntity.getFirstName());
        entity.setLastName(newEntity.getLastName());
        User saved = this.repository.save(entity);
        //eventPublisher.publishEvent(new UserNameChangedEvent(saved.getId(), saved.getUsername(), saved.getFirstName(), saved.getLastName()));

        return this.assembler.toModel(saved.toDto());
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        User entity = this.repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

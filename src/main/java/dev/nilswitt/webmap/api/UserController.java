package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.UserNotFoundException;
import dev.nilswitt.webmap.api.exceptions.UserNotFoundException;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.security.PermissionUtil;
import org.springframework.context.ApplicationEventPublisher;
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
    CollectionModel<EntityModel<User>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.USER)) {

            List<EntityModel<User>> entities = this.repository.findAll().stream()
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(UserController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionUtil.getUsersForUser(userDetails).stream().map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(UserController.class).all(null)).withSelfRel());

    }

    @PostMapping("")
    EntityModel<User> newEmployee(@RequestBody User newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.USER)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<User> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        User entity = this.repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        return this.assembler.toModel(entity        );
    }

    @PutMapping("{id}")
    EntityModel<User> replaceEntity(@RequestBody User newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
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

        return this.assembler.toModel(saved);
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

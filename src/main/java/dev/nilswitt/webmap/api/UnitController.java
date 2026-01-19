package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.UnitNotFoundException;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
import dev.nilswitt.webmap.security.PermissionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@RequestMapping("api/units")
public class UnitController {

    private final UnitRepository repository;
    private final UnitModelAssembler assembler;
    private final PermissionUtil permissionUtil;
    private final Logger logger = LogManager.getLogger(this.getClass());

    public UnitController(UnitRepository userRepository, UnitModelAssembler assembler, PermissionUtil permissionUtil) {
        this.repository = userRepository;
        this.assembler = assembler;
        this.permissionUtil = permissionUtil;
    }

    @GetMapping("")
    CollectionModel<EntityModel<Unit>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.UNIT)) {

            List<EntityModel<Unit>> entities = this.repository.findAll().stream()
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(UnitController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionUtil.getUnitsForUser(userDetails).stream().map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(UnitController.class).all(null)).withSelfRel());
    }

    @PostMapping("")
    EntityModel<Unit> newEntity(@RequestBody Unit newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.UNIT)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<Unit> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new UnitNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<Unit> replaceEntity(@RequestBody Unit newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setName(newEntity.getName());
        entity.setPosition(newEntity.getPosition());
        entity.setStatus(newEntity.getStatus());
        entity.setSpeakRequest(newEntity.isSpeakRequest());

        return this.assembler.toModel(this.repository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

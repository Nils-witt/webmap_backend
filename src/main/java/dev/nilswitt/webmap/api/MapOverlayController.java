package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.MapOverlayNotFoundException;
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapOverlayRepository;
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
@RequestMapping("api/map/overlays")
public class MapOverlayController {

    private final MapOverlayRepository repository;
    private final MapOverlayModelAssembler assembler;
    private final PermissionUtil permissionUtil;

    public MapOverlayController(MapOverlayRepository repository, MapOverlayModelAssembler assembler, PermissionUtil permissionUtil) {
        this.repository = repository;
        this.assembler = assembler;
        this.permissionUtil = permissionUtil;
    }

    @GetMapping("")
    CollectionModel<EntityModel<MapOverlay>> all(@AuthenticationPrincipal User userDetails) {

        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY)) {

            List<EntityModel<MapOverlay>> entities = this.repository.findAll().stream()
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(MapOverlayController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionUtil.getMapOverlaysForUser(userDetails).stream().map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(MapOverlayController.class).all(null)).withSelfRel());
    }


    @PostMapping("")
    EntityModel<MapOverlay> newEntity(@RequestBody MapOverlay newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<MapOverlay> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapOverlay entity = this.repository.findById(id).orElseThrow(() -> new MapOverlayNotFoundException(id));
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        return this.assembler.toModel(entity);
    }

    @PutMapping("{id}")
    EntityModel<MapOverlay> replaceEntity(@RequestBody MapOverlay newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapOverlay entity = this.repository.findById(id).orElseThrow(() -> new MapOverlayNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setName(newEntity.getName());
        entity.setBasePath(newEntity.getBasePath());
        entity.setTilePathPattern(newEntity.getTilePathPattern());

        return this.assembler.toModel(this.repository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapOverlay entity = this.repository.findById(id).orElseThrow(() -> new MapOverlayNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

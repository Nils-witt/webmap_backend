package dev.nilswitt.webmap.api.rest.v1.map;

import dev.nilswitt.webmap.api.dtos.MapOverlayDto;
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapOverlayRepository;
import dev.nilswitt.webmap.exceptions.ForbiddenException;
import dev.nilswitt.webmap.exceptions.MapOverlayNotFoundException;
import dev.nilswitt.webmap.security.PermissionVerifier;
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
    private final PermissionVerifier permissionVerifier;

    public MapOverlayController(MapOverlayRepository repository, MapOverlayModelAssembler assembler, PermissionVerifier permissionVerifier) {
        this.repository = repository;
        this.assembler = assembler;
        this.permissionVerifier = permissionVerifier;
    }

    @GetMapping("")
    CollectionModel<EntityModel<MapOverlayDto>> all(@AuthenticationPrincipal User userDetails) {

        if (this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY)) {

            List<EntityModel<MapOverlayDto>> entities = this.repository.findAll().stream()
                    .map(mapOverlay -> {
                        MapOverlayDto dto = mapOverlay.toDto();
                        dto.setPermissions(this.permissionVerifier.getScopes(mapOverlay, userDetails));
                        return dto;
                    })
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(MapOverlayController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionVerifier.getMapOverlaysForUser(userDetails).stream().map(mapOverlay -> {
            MapOverlayDto dto = mapOverlay.toDto();
            dto.setPermissions(this.permissionVerifier.getScopes(mapOverlay, userDetails));
            return dto;
        }).map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(MapOverlayController.class).all(null)).withSelfRel());
    }


    @PostMapping("")
    EntityModel<MapOverlayDto> newEntity(@RequestBody MapOverlayDto newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        MapOverlay entity = this.repository.save(MapOverlay.of(newEntity));
        MapOverlayDto dto = entity.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(entity, userDetails));
        return this.assembler.toModel(dto);
    }

    @GetMapping("{id}")
    EntityModel<MapOverlayDto> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapOverlay entity = this.repository.findById(id).orElseThrow(() -> new MapOverlayNotFoundException(id));
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        MapOverlayDto dto = entity.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(entity, userDetails));
        return this.assembler.toModel(dto);
    }

    @PutMapping("{id}")
    EntityModel<MapOverlayDto> replaceEntity(@RequestBody MapOverlayDto newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapOverlay entity = this.repository.findById(id).orElseThrow(() -> new MapOverlayNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setName(newEntity.getName());

        entity.setBasePath(newEntity.getBasePath());
        entity.setBasePath(newEntity.getBasePath());
        entity.setTilePathPattern(newEntity.getTilePathPattern());

        MapOverlay saved = this.repository.save(entity);
        MapOverlayDto dto = saved.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(saved, userDetails));
        return this.assembler.toModel(dto);
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapOverlay entity = this.repository.findById(id).orElseThrow(() -> new MapOverlayNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

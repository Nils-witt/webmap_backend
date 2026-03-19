package dev.nilswitt.webmap.api.rest.v1.map;

import dev.nilswitt.webmap.api.dtos.MapBaseLayerDto;
import dev.nilswitt.webmap.entities.MapBaseLayer;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapBaseLayerRepository;
import dev.nilswitt.webmap.exceptions.ForbiddenException;
import dev.nilswitt.webmap.exceptions.MapBaseLayerNotFoundException;
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
@RequestMapping("api/map/baselayers")
public class MapBaseLayerController {

    private final MapBaseLayerRepository repository;
    private final MapBaseLayerModelAssembler assembler;
    private final PermissionVerifier permissionVerifier;

    public MapBaseLayerController(MapBaseLayerRepository repository, MapBaseLayerModelAssembler assembler, PermissionVerifier permissionVerifier) {
        this.repository = repository;
        this.assembler = assembler;
        this.permissionVerifier = permissionVerifier;
    }

    @GetMapping("")
    CollectionModel<EntityModel<MapBaseLayerDto>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY)) {

            List<EntityModel<MapBaseLayerDto>> entities = this.repository.findAll().stream()
                    .map(mapBaseLayer -> {
                        MapBaseLayerDto dto = mapBaseLayer.toDto();
                        dto.setPermissions(this.permissionVerifier.getScopes(mapBaseLayer, userDetails));
                        return dto;
                    })
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(MapBaseLayerController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionVerifier.getMapBaseLayersForUser(userDetails).stream().map(mapBaseLayer -> {
            MapBaseLayerDto dto = mapBaseLayer.toDto();
            dto.setPermissions(this.permissionVerifier.getScopes(mapBaseLayer, userDetails));
            return dto;
        }).map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(MapBaseLayerController.class).all(null)).withSelfRel());
    }

    @PostMapping("")
    EntityModel<MapBaseLayerDto> newEntity(@RequestBody MapBaseLayerDto newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        MapBaseLayer entity = this.repository.save(MapBaseLayer.of(newEntity));
        MapBaseLayerDto dto = entity.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(entity, userDetails));
        return this.assembler.toModel(dto);
    }

    @GetMapping("{id}")
    EntityModel<MapBaseLayerDto> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapBaseLayer entity = this.repository.findById(id).orElseThrow(() -> new MapBaseLayerNotFoundException(id));
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        MapBaseLayerDto dto = entity.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(entity, userDetails));
        return this.assembler.toModel(dto);
    }

    @PutMapping("{id}")
    EntityModel<MapBaseLayerDto> replaceEntity(@RequestBody MapBaseLayerDto newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapBaseLayer entity = this.repository.findById(id).orElseThrow(() -> new MapBaseLayerNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setName(newEntity.getName());
        entity.setUrl(newEntity.getUrl());
        entity.setCacheUrl(newEntity.getCacheUrl());

        MapBaseLayer saved = this.repository.save(entity);
        MapBaseLayerDto dto = saved.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(saved, userDetails));
        return this.assembler.toModel(dto);
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapBaseLayer entity = this.repository.findById(id).orElseThrow(() -> new MapBaseLayerNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

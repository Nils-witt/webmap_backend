package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.api.dtos.MapBaseLayerDto;
import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.MapBaseLayerNotFoundException;
import dev.nilswitt.webmap.entities.MapBaseLayer;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapBaseLayerRepository;
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
@RequestMapping("api/map/baselayers")
public class MapBaseLayerController {

    private final MapBaseLayerRepository repository;
    private final MapBaseLayerModelAssembler assembler;
    private final PermissionUtil permissionUtil;

    public MapBaseLayerController(MapBaseLayerRepository repository, MapBaseLayerModelAssembler assembler, PermissionUtil permissionUtil) {
        this.repository = repository;
        this.assembler = assembler;
        this.permissionUtil = permissionUtil;
    }

    @GetMapping("")
    CollectionModel<EntityModel<MapBaseLayerDto>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY)) {

            List<EntityModel<MapBaseLayerDto>> entities = this.repository.findAll().stream()
                    .map(MapBaseLayer::toDto)
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(MapBaseLayerController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionUtil.getMapBaseLayersForUser(userDetails).stream().map(MapBaseLayer::toDto).map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(MapBaseLayerController.class).all(null)).withSelfRel());
    }

    @PostMapping("")
    EntityModel<MapBaseLayerDto> newEntity(@RequestBody MapBaseLayerDto newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        return this.assembler.toModel(this.repository.save(MapBaseLayer.of(newEntity)).toDto());
    }

    @GetMapping("{id}")
    EntityModel<MapBaseLayerDto> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapBaseLayer entity = this.repository.findById(id).orElseThrow(() -> new MapBaseLayerNotFoundException(id));
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        return this.assembler.toModel(entity.toDto());
    }

    @PutMapping("{id}")
    EntityModel<MapBaseLayerDto> replaceEntity(@RequestBody MapBaseLayerDto newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapBaseLayer entity = this.repository.findById(id).orElseThrow(() -> new MapBaseLayerNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        return this.assembler.toModel(this.repository.save(entity).toDto());
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapBaseLayer entity = this.repository.findById(id).orElseThrow(() -> new MapBaseLayerNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.api.dtos.MapItemDto;
import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.MapItemNotFoundException;
import dev.nilswitt.webmap.entities.EmbeddedPosition;
import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapGroupRepository;
import dev.nilswitt.webmap.entities.repositories.MapItemRepository;
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
@RequestMapping("api/map/items")
public class MapItemController {

    private final MapItemRepository repository;
    private final MapItemModelAssembler assembler;
    private final PermissionUtil permissionUtil;
    private final MapGroupRepository mapGroupRepository;

    public MapItemController(MapItemRepository repository, MapItemModelAssembler assembler, PermissionUtil permissionUtil, MapGroupRepository mapGroupRepository) {
        this.repository = repository;
        this.permissionUtil = permissionUtil;
        this.assembler = assembler;
        this.mapGroupRepository = mapGroupRepository;
    }

    @GetMapping("")
    CollectionModel<EntityModel<MapItemDto>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {

            List<EntityModel<MapItemDto>> entities = this.repository.findAll().stream()
                    .map(mapItem -> this.assembler.toModel(mapItem.toDto()))
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(MapItemController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionUtil.getMapItemsForUser(userDetails).stream().map(mapItem -> this.assembler.toModel(mapItem.toDto())).collect(Collectors.toList()), linkTo(methodOn(MapItemController.class).all(null)).withSelfRel());

    }

    @PostMapping("")
    EntityModel<MapItemDto> newEntity(@RequestBody MapItemDto newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }

        MapItem mapItem = new MapItem();
        mapItem.setName(newEntity.getName());
        mapItem.setPosition(EmbeddedPosition.of(newEntity.getPosition()));

        return this.assembler.toModel(this.repository.save(mapItem).toDto());
    }

    @GetMapping("{id}")
    EntityModel<MapItemDto> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapItem entity = this.repository.findById(id).orElseThrow(() -> new MapItemNotFoundException(id));
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        return this.assembler.toModel(
                (this.repository.findById(id)
                        .orElseThrow(() -> new MapItemNotFoundException(id))).toDto()
        );
    }

    @PutMapping("{id}")
    EntityModel<MapItemDto> replaceEntity(@RequestBody MapItemDto newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapItem entity = this.repository.findById(id).orElseThrow(() -> new MapItemNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setName(newEntity.getName());
        entity.setPosition(EmbeddedPosition.of(newEntity.getPosition()));

        if (newEntity.getMapGroupId() != null) {
            entity.setMapGroup(mapGroupRepository.findById(newEntity.getMapGroupId()).orElseThrow());
        } else {
            entity.setMapGroup(null);
        }

        return this.assembler.toModel(this.repository.save(entity).toDto());
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapItem entity = this.repository.findById(id).orElseThrow(() -> new MapItemNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

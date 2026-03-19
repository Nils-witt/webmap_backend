package dev.nilswitt.webmap.api.rest.v1.map;

import dev.nilswitt.webmap.api.dtos.MapItemDto;
import dev.nilswitt.webmap.entities.EmbeddedPosition;
import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapGroupRepository;
import dev.nilswitt.webmap.entities.repositories.MapItemRepository;
import dev.nilswitt.webmap.exceptions.ForbiddenException;
import dev.nilswitt.webmap.exceptions.MapItemNotFoundException;
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
@RequestMapping("api/map/items")
public class MapItemController {

    private final MapItemRepository repository;
    private final MapItemModelAssembler assembler;
    private final PermissionVerifier permissionVerifier;
    private final MapGroupRepository mapGroupRepository;

    public MapItemController(MapItemRepository repository, MapItemModelAssembler assembler, PermissionVerifier permissionVerifier, MapGroupRepository mapGroupRepository) {
        this.repository = repository;
        this.permissionVerifier = permissionVerifier;
        this.assembler = assembler;
        this.mapGroupRepository = mapGroupRepository;
    }

    @GetMapping("")
    CollectionModel<EntityModel<MapItemDto>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {

            List<EntityModel<MapItemDto>> entities = this.repository.findAll().stream()
                    .map(mapItem -> {
                        MapItemDto dto = mapItem.toDto();
                        dto.setPermissions(this.permissionVerifier.getScopes(mapItem, userDetails));
                        return dto;
                    })
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(MapItemController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionVerifier.getMapItemsForUser(userDetails).stream().map(mapItem -> {
            MapItemDto dto = mapItem.toDto();
            dto.setPermissions(this.permissionVerifier.getScopes(mapItem, userDetails));
            return dto;
        }).map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(MapItemController.class).all(null)).withSelfRel());

    }

    @PostMapping("")
    EntityModel<MapItemDto> newEntity(@RequestBody MapItemDto newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        MapItem saved = this.repository.save(MapItem.of(newEntity));
        MapItemDto dto = saved.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(saved, userDetails));
        return this.assembler.toModel(dto);
    }

    @GetMapping("{id}")
    EntityModel<MapItemDto> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapItem entity = this.repository.findById(id).orElseThrow(() -> new MapItemNotFoundException(id));
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }

        MapItemDto dto = entity.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(entity, userDetails));
        return this.assembler.toModel(dto);
    }

    @PutMapping("{id}")
    EntityModel<MapItemDto> replaceEntity(@RequestBody MapItemDto newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapItem entity = this.repository.findById(id).orElseThrow(() -> new MapItemNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setName(newEntity.getName());
        entity.setPosition(EmbeddedPosition.of(newEntity.getPosition()));
        entity.setZoomLevel(newEntity.getZoomLevel());

        if (newEntity.getMapGroupId() != null) {
            entity.setMapGroup(mapGroupRepository.findById(newEntity.getMapGroupId()).orElseThrow());
        } else {
            entity.setMapGroup(null);
        }

        MapItem saved = this.repository.save(entity);
        MapItemDto dto = saved.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(saved, userDetails));
        return this.assembler.toModel(dto);
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapItem entity = this.repository.findById(id).orElseThrow(() -> new MapItemNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

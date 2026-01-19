package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.MapItemNotFoundException;
import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
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
    private  final PermissionUtil permissionUtil;

    public MapItemController(MapItemRepository repository, MapItemModelAssembler assembler, PermissionUtil permissionUtil) {
        this.repository = repository;
        this.permissionUtil = permissionUtil;
        this.assembler = assembler;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<MapItem>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {

            List<EntityModel<MapItem>> entities = this.repository.findAll().stream()
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(MapItemController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionUtil.getMapItemsForUser(userDetails).stream().map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(MapItemController.class).all(null)).withSelfRel());

    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<MapItem> newEntity(@RequestBody MapItem newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<MapItem> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapItem entity = this.repository.findById(id).orElseThrow(() -> new MapItemNotFoundException(id));
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new MapItemNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<MapItem> replaceEntity(@RequestBody MapItem newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        MapItem entity = this.repository.findById(id).orElseThrow(() -> new MapItemNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setName(newEntity.getName());
        entity.setPosition(newEntity.getPosition());

        return this.assembler.toModel(this.repository.save(entity));
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

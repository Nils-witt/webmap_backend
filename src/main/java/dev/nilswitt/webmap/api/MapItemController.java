package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.MapItemNotFoundException;
import dev.nilswitt.webmap.api.helpers.ApiAuthorizationHelper;
import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapItemRepository;
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

    public MapItemController(MapItemRepository repository, MapItemModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<MapItem>> all(@AuthenticationPrincipal User userDetails) {
        try {
            ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPITEM,
                    "User does not have permission to view map items.", SecurityGroup.UserRoleScopeEnum.VIEW);

            List<EntityModel<MapItem>> items = this.repository.findAll().stream()
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());

            return CollectionModel.of(items, linkTo(methodOn(MapItemController.class).all(null)).withSelfRel());
        } catch (ForbiddenException e) {
            return CollectionModel.of(List.of(), linkTo(methodOn(MapItemController.class).all(null)).withSelfRel());
        }

    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<MapItem> newEntity(@RequestBody MapItem newEntity, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPITEM,
                "User does not have permission to create map items.", SecurityGroup.UserRoleScopeEnum.CREATE);

        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<MapItem> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPITEM,
                "User does not have permission to view map items.", SecurityGroup.UserRoleScopeEnum.VIEW);

        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new MapItemNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<MapItem> replaceEntity(@RequestBody MapItem newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPITEM,
                "User does not have permission to edit map items.", SecurityGroup.UserRoleScopeEnum.EDIT);

        MapItem entity = this.repository.findById(id)
                .orElseThrow(() -> new MapItemNotFoundException(id));

        entity.setName(newEntity.getName());
        entity.setPosition(newEntity.getPosition());

        return this.assembler.toModel(this.repository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPITEM,
                "User does not have permission to delete map items.", SecurityGroup.UserRoleScopeEnum.DELETE);

        this.repository.deleteById(id);
    }
}

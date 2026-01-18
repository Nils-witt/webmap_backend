package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.MapOverlayNotFoundException;
import dev.nilswitt.webmap.api.helpers.ApiAuthorizationHelper;
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapOverlayRepository;
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

    public MapOverlayController(MapOverlayRepository repository, MapOverlayModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<MapOverlay>> all(@AuthenticationPrincipal User userDetails) {
        try {
            ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY,
                    "User does not have permission to view overlays.", SecurityGroup.UserRoleScopeEnum.VIEW);

            List<EntityModel<MapOverlay>> entities = this.repository.findAll().stream()
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());

            return CollectionModel.of(entities, linkTo(methodOn(MapOverlayController.class).all(null)).withSelfRel());
        } catch (ForbiddenException e) {
            return CollectionModel.of(List.of(), linkTo(methodOn(MapOverlayController.class).all(null)).withSelfRel());
        }
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<MapOverlay> newEntity(@RequestBody MapOverlay newEntity, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY,
                "User does not have permission to create overlays.", SecurityGroup.UserRoleScopeEnum.CREATE);

        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<MapOverlay> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY,
                "User does not have permission to view overlays.", SecurityGroup.UserRoleScopeEnum.VIEW);

        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new MapOverlayNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<MapOverlay> replaceEntity(@RequestBody MapOverlay newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY,
                "User does not have permission to edit overlays.", SecurityGroup.UserRoleScopeEnum.EDIT);

        MapOverlay entity = this.repository.findById(id)
                .orElseThrow(() -> new MapOverlayNotFoundException(id));

        entity.setName(newEntity.getName());

        return this.assembler.toModel(this.repository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY,
                "User does not have permission to delete overlays.", SecurityGroup.UserRoleScopeEnum.DELETE);

        this.repository.deleteById(id);
    }
}

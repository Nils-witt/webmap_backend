package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.UnitNotFoundException;
import dev.nilswitt.webmap.api.helpers.ApiAuthorizationHelper;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
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
    private final Logger logger = LogManager.getLogger(this.getClass());

    public UnitController(UnitRepository userRepository, UnitModelAssembler assembler) {
        this.repository = userRepository;
        this.assembler = assembler;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<Unit>> all(@AuthenticationPrincipal User userDetails) {

        try {
            ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.UNIT,
                    "User does not have permission to create users.", SecurityGroup.UserRoleScopeEnum.VIEW);
            List<EntityModel<Unit>> units = this.repository.findAll().stream()
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(units, linkTo(methodOn(UnitController.class).all(null)).withSelfRel());

        } catch (ForbiddenException e) {
            return CollectionModel.of(List.of(), linkTo(methodOn(UnitController.class).all(null)).withSelfRel());

        }
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<Unit> newEntity(@RequestBody Unit newEntity, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.UNIT,
                "User does not have permission to create users.", SecurityGroup.UserRoleScopeEnum.CREATE);
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<Unit> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.UNIT,
                "User does not have permission to view units.", SecurityGroup.UserRoleScopeEnum.VIEW);
        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new UnitNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<Unit> replaceEntity(@RequestBody Unit newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.UNIT,
                "User does not have permission to edit unit.", SecurityGroup.UserRoleScopeEnum.EDIT);
        Unit entity = this.repository.findById(id)
                .orElseThrow(() -> new UnitNotFoundException(id));

        entity.setName(newEntity.getName());
        entity.setPosition(newEntity.getPosition());
        entity.setStatus(newEntity.getStatus());
        entity.setSpeakRequest(newEntity.isSpeakRequest());

        return this.assembler.toModel(this.repository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        ApiAuthorizationHelper.requireAnyScope(userDetails, SecurityGroup.UserRoleTypeEnum.UNIT,
                "User does not have permission to delete units.", SecurityGroup.UserRoleScopeEnum.DELETE);
        this.repository.deleteById(id);
    }
}

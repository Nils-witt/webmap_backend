package dev.nilswitt.webmap.api.controller;

import dev.nilswitt.webmap.api.dtos.EmbeddedPositionDto;
import dev.nilswitt.webmap.api.dtos.UnitDto;
import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.api.exceptions.UnitNotFoundException;
import dev.nilswitt.webmap.entities.*;
import dev.nilswitt.webmap.entities.repositories.UnitPositionLogRepository;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
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
@RequestMapping("api/units")
public class UnitController {

    private final UnitRepository repository;
    private final UnitModelAssembler assembler;
    private final PermissionUtil permissionUtil;
    private final UnitPositionLogRepository unitPositionLogRepository;

    public UnitController(UnitRepository userRepository, UnitModelAssembler assembler, PermissionUtil permissionUtil, UnitPositionLogRepository unitPositionLogRepository) {
        this.repository = userRepository;
        this.assembler = assembler;
        this.permissionUtil = permissionUtil;
        this.unitPositionLogRepository = unitPositionLogRepository;
    }

    @GetMapping("")
    CollectionModel<EntityModel<UnitDto>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.UNIT)) {

            List<EntityModel<UnitDto>> entities = this.repository.findAll().stream()
                    .map(Unit::toDto)
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(UnitController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionUtil.getUnitsForUser(userDetails).stream().map(Unit::toDto).map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(UnitController.class).all(null)).withSelfRel());
    }

    @PostMapping("")
    EntityModel<UnitDto> newEntity(@RequestBody UnitDto newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.UNIT)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }
        return this.assembler.toModel(this.repository.save(Unit.of(newEntity)).toDto());
    }

    @GetMapping("{id}")
    EntityModel<UnitDto> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));
        if (this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }
        return this.assembler.toModel(
                (this.repository.findById(id)
                        .orElseThrow(() -> new UnitNotFoundException(id))).toDto()
        );
    }

    @PutMapping("{id}")
    EntityModel<UnitDto> replaceEntity(@RequestBody UnitDto newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }

        entity.setName(newEntity.getName());
        entity.setPosition(EmbeddedPosition.of(newEntity.getPosition()));
        entity.setStatus(newEntity.getStatus());
        entity.setSpeakRequest(newEntity.isSpeakRequest());

        return this.assembler.toModel(this.repository.save(entity).toDto());
    }

    @PostMapping("{id}/position")
    EntityModel<UnitDto> replaceEntity(@RequestBody EmbeddedPositionDto newEntity, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            throw new ForbiddenException("User does not have permission to edit overlays.");
        }
        // Check if postion has a newer timestamp
        if (entity.getPosition().getTimestamp().isBefore(newEntity.getTimestamp())) {
            entity.setPosition(EmbeddedPosition.of(newEntity));

            UnitPositionLog log = new UnitPositionLog();
            log.setUnit(entity);
            log.setPosition(entity.getPosition());
            this.unitPositionLogRepository.save(log);
        }

        return this.assembler.toModel(this.repository.save(entity).toDto());
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));

        if (!this.permissionUtil.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }
}

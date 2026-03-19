package dev.nilswitt.webmap.api.rest.v1;

import dev.nilswitt.webmap.api.dtos.TacticalIconDto;
import dev.nilswitt.webmap.api.dtos.UnitDto;
import dev.nilswitt.webmap.api.dtos.UnitPositionLogDto;
import dev.nilswitt.webmap.api.dtos.UnitStatusDto;
import dev.nilswitt.webmap.entities.*;
import dev.nilswitt.webmap.entities.repositories.UnitPositionLogRepository;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
import dev.nilswitt.webmap.entities.repositories.UnitStatusUpdateRepository;
import dev.nilswitt.webmap.exceptions.ForbiddenException;
import dev.nilswitt.webmap.exceptions.UnitNotFoundException;
import dev.nilswitt.webmap.security.PermissionVerifier;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api/units")
@Log4j2
public class UnitController {

    private final UnitRepository repository;
    private final UnitModelAssembler assembler;
    private final UnitStatusUpdateModelAssembler unitStatusUpdateModelAssembler;
    private final UnitPositionLogModelAssembler unitPositionLogModelAssembler;
    private final PermissionVerifier permissionVerifier;
    private final UnitStatusUpdateRepository unitStatusUpdateRepository;
    private final UnitPositionLogRepository unitPositionLogRepository;

    public UnitController(UnitRepository userRepository, UnitModelAssembler assembler, UnitStatusUpdateModelAssembler unitStatusUpdateModelAssembler, PermissionVerifier permissionVerifier, UnitStatusUpdateRepository unitStatusUpdateRepository, UnitPositionLogRepository unitPositionLogRepository, UnitPositionLogModelAssembler unitPositionLogModelAssembler) {
        this.repository = userRepository;
        this.assembler = assembler;
        this.permissionVerifier = permissionVerifier;
        this.unitStatusUpdateRepository = unitStatusUpdateRepository;
        this.unitStatusUpdateModelAssembler = unitStatusUpdateModelAssembler;
        this.unitPositionLogRepository = unitPositionLogRepository;
        this.unitPositionLogModelAssembler = unitPositionLogModelAssembler;
    }

    @GetMapping("")
    CollectionModel<EntityModel<UnitDto>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.UNIT)) {

            List<EntityModel<UnitDto>> entities = this.repository.findAll().stream()
                    .map(unit -> {
                        UnitDto dto = unit.toDto();
                        dto.setPermissions(this.permissionVerifier.getScopes(unit, userDetails));
                        return dto;
                    })
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(UnitController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionVerifier.getUnitsForUser(userDetails).stream().map(unit -> {
            UnitDto dto = unit.toDto();
            dto.setPermissions(this.permissionVerifier.getScopes(unit, userDetails));
            return dto;
        }).map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(UnitController.class).all(null)).withSelfRel());
    }

    @PostMapping("")
    EntityModel<UnitDto> newEntity(@RequestBody UnitDto newEntity, @AuthenticationPrincipal User userDetails) {
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.UNIT)) {
            throw new ForbiddenException("User does not have permission to create overlays.");
        }

        Unit unit = this.repository.save(Unit.of(newEntity));

        UnitStatusUpdate statusUpdate = UnitStatusUpdate.builder()
                .status(newEntity.getStatus())
                .acknowledged(false)
                .unit(unit)
                .build();

        unitStatusUpdateRepository.save(statusUpdate);

        UnitDto dto = unit.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(unit, userDetails));
        return this.assembler.toModel(dto);
    }

    @GetMapping("{id}")
    EntityModel<UnitDto> one(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view overlays.");
        }

        Unit unit = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));
        UnitDto dto = unit.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(unit, userDetails));
        return this.assembler.toModel(dto);
    }

    @PatchMapping("{id}")
    EntityModel<UnitDto> updateEntity(@RequestBody String rawBody, @PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.EDIT, entity)) {
            // When it is an Unit user allow some self updates
            if (userDetails.getUnit().getId().equals(entity.getId())) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode data = mapper.readTree(rawBody);
                    if (data.has("status")) {
                        int newStatus = data.get("status").asInt();
                        if (newStatus == 5) {
                            entity.setSpeakRequest(true);
                        } else {
                            entity.setStatus(newStatus);
                        }
                        log.info("Commited status update: {}", entity.getStatus());
                    }

                } catch (Exception e) {
                    log.error("Failed to parse JSON body: {}", e.getMessage(), e);
                }
            } else {
                throw new ForbiddenException("User does not have permission to edit overlays.");
            }
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(rawBody);
            if (data.has("name")) {
                entity.setName(data.get("name").asString());
            }
            if (data.has("position")) {
                JsonNode positionNode = data.get("position");
                if (positionNode.isObject()) {
                    if (positionNode.has("latitude") && positionNode.has("longitude")) {
                        try {
                            EmbeddedPosition position = new EmbeddedPosition();
                            position.setLatitude(positionNode.get("latitude").doubleValue());
                            position.setLongitude(positionNode.get("longitude").doubleValue());

                            if (positionNode.has("altitude")) {
                                position.setAltitude(positionNode.get("altitude").doubleValue());
                            } else {
                                position.setAltitude(0.0);
                            }
                            if (positionNode.has("accuracy")) {
                                position.setAccuracy(positionNode.get("accuracy").doubleValue());
                            } else {
                                position.setAccuracy(-1.0);
                            }

                            if (positionNode.has("timestamp")) {
                                try {
                                    position.setTimestamp(Instant.parse(positionNode.get("timestamp").asString()));
                                } catch (Exception e) {
                                    log.warn("Could not parse timestamp as date: {}", positionNode.get("timestamp"));
                                    position.setTimestamp(Instant.now());
                                }
                            } else {
                                position.setTimestamp(Instant.now());
                            }
                            entity.setPosition(position);
                            UnitPositionLog logEntry = new UnitPositionLog(entity, position);
                            unitPositionLogRepository.save(logEntry);

                        } catch (Exception e) {
                            /* Pass */
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
            if (data.has("speakRequest")) {
                boolean speakRequest = data.get("speakRequest").asBoolean();
                if (!speakRequest) {
                    entity.setSpeakRequest(false);
                }
            }
            if (data.has("status")) {
                int newStatus = data.get("status").asInt();
                if (newStatus == 5) {
                    entity.setSpeakRequest(true);
                } else {
                    entity.setStatus(newStatus);
                }
                log.info("Commited status update: {}", entity.getStatus());
                unitStatusUpdateRepository.save(UnitStatusUpdate.builder().status(entity.getStatus()).acknowledged(false).unit(entity).build());
            }
            if (data.has("icon")) {
                TacticalIconDto iconDto = mapper.treeToValue(data.get("icon"), TacticalIconDto.class);
                entity.setIcon(TacticalIcon.of(iconDto));
            }

        } catch (Exception e) {
            log.error("Failed to parse JSON body: {}", e.getMessage(), e);
        }
        Unit unit = this.repository.save(entity);
        UnitDto dto = unit.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(unit, userDetails));
        return this.assembler.toModel(dto);
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete overlays.");
        }
        this.repository.deleteById(id);
    }


    @GetMapping("{id}/status/history")
    CollectionModel<EntityModel<UnitStatusDto>> getStatusHistory(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view that unit.");
        }
        return CollectionModel.of(this.unitStatusUpdateRepository.findByUnit(entity).stream().map(UnitStatusUpdate::toDto).map(this.unitStatusUpdateModelAssembler::toModel).toList(), linkTo(methodOn(UnitController.class).all(null)).withSelfRel());
    }

    @GetMapping("{id}/position/history")
    CollectionModel<EntityModel<UnitPositionLogDto>> getPositionHistory(@PathVariable UUID id, @RequestParam(required = false) Integer since, @AuthenticationPrincipal User userDetails) {
        Unit entity = this.repository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view that unit.");
        }

        if (since == null) {
            return CollectionModel.of(this.unitPositionLogRepository.findByUnit(entity).stream().map(UnitPositionLog::toDto).map(this.unitPositionLogModelAssembler::toModel).toList(), linkTo(methodOn(UnitController.class).all(null)).withSelfRel());
        } else {
            Instant sinceInstant = Instant.ofEpochSecond(since);
            return CollectionModel.of(this.unitPositionLogRepository.findByUnitAndPosition_TimestampAfter(entity, sinceInstant).stream().map(UnitPositionLog::toDto).map(this.unitPositionLogModelAssembler::toModel).toList(), linkTo(methodOn(UnitController.class).all(null)).withSelfRel());

        }
    }
}

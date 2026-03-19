package dev.nilswitt.webmap.api.rest.v1;

import dev.nilswitt.webmap.api.dtos.PhotoDto;
import dev.nilswitt.webmap.entities.*;
import dev.nilswitt.webmap.entities.repositories.MissionGroupRepository;
import dev.nilswitt.webmap.entities.repositories.PhotoRepository;
import dev.nilswitt.webmap.exceptions.ForbiddenException;
import dev.nilswitt.webmap.exceptions.PhotoNotFoundException;
import dev.nilswitt.webmap.records.PictureConfig;
import dev.nilswitt.webmap.security.PermissionVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("api/photos")
public class PhotoController {
    private final PhotoModelAssembler assembler;
    private final PhotoRepository photoRepository;
    private final PictureConfig pictureConfig;
    private final PermissionVerifier permissionVerifier;
    private final MissionGroupRepository missionGroupRepository;


    public PhotoController(PhotoModelAssembler assembler, PhotoRepository photoRepository, PictureConfig pictureConfig, PermissionVerifier permissionVerifier, MissionGroupRepository missionGroupRepository) {
        this.assembler = assembler;
        this.photoRepository = photoRepository;
        this.pictureConfig = pictureConfig;
        this.permissionVerifier = permissionVerifier;
        this.missionGroupRepository = missionGroupRepository;
    }

    @GetMapping("")
    CollectionModel<EntityModel<PhotoDto>> all(@AuthenticationPrincipal User userDetails) {
        if (this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, SecurityGroup.UserRoleTypeEnum.PHOTO)) {
            List<EntityModel<PhotoDto>> entities = this.photoRepository.findAll().stream()
                    .map(photo -> {
                        PhotoDto dto = photo.toDto();
                        dto.setPermissions(this.permissionVerifier.getScopes(photo, userDetails));
                        return dto;
                    })
                    .map(this.assembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(entities, linkTo(methodOn(PhotoController.class).all(null)).withSelfRel());
        }

        return CollectionModel.of(this.permissionVerifier.getPhotosForUser(userDetails).stream().map(entity -> {
            PhotoDto dto = entity.toDto();
            dto.setPermissions(this.permissionVerifier.getScopes(entity, userDetails));
            return dto;
        }).map(this.assembler::toModel).collect(Collectors.toList()), linkTo(methodOn(PhotoController.class).all(null)).withSelfRel());
    }

    @PostMapping("")
    EntityModel<PhotoDto> newEntity(
            @RequestParam("file") MultipartFile file,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("missionGroupId") UUID missionGroupId,
            @AuthenticationPrincipal User userDetails) {

        MissionGroup missionGroup = this.missionGroupRepository.findById(missionGroupId).orElseThrow(() -> new PhotoNotFoundException(missionGroupId));

        if (userDetails.getUnit() != null) {
            if (!userDetails.getUnit().getMissionGroup().getId().equals(missionGroup.getId())) {
                if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.PHOTO)) {
                    throw new ForbiddenException("User does not have permission to create photos.");
                }
            }
        } else {
            if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.PHOTO)) {
                throw new ForbiddenException("User does not have permission to create photos.");
            }
        }

        EmbeddedPosition position = new EmbeddedPosition();
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        position.setAccuracy(0.0);
        position.setTimestamp(Instant.now());

        Photo newPhoto = new Photo();
        newPhoto.setAuthor(userDetails);
        newPhoto.setMissionGroup(missionGroupRepository.findById(missionGroupId).orElse(null));
        newPhoto.setPosition(position);
        newPhoto = this.photoRepository.save(newPhoto);
        String fileExtension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        try {

            String dailyPath = pictureConfig.localPath() + "/" + LocalDateTime.now().getYear() + "/" + LocalDateTime.now().getMonthValue() + "/" + LocalDateTime.now().getDayOfMonth();
            Files.createDirectories(Path.of(dailyPath));

            String filePath = dailyPath + "/" + newPhoto.getId() + fileExtension;
            FileOutputStream fos = new FileOutputStream(filePath);
            file.getInputStream().transferTo(fos);
            fos.close();
            newPhoto.setPath(LocalDateTime.now().getYear() + "/" + LocalDateTime.now().getMonthValue() + "/" + LocalDateTime.now().getDayOfMonth() + "/" + newPhoto.getId() + fileExtension);
            newPhoto.setName("Photo " + newPhoto.getId());

            photoRepository.save(newPhoto);
            PhotoDto dto = newPhoto.toDto();
            dto.setPermissions(this.permissionVerifier.getScopes(newPhoto, userDetails));
            return this.assembler.toModel(dto);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @PatchMapping("{id}")
    EntityModel<PhotoDto> updateEntity(@PathVariable UUID id, @RequestBody String rawBody, @AuthenticationPrincipal User userDetails) throws IOException {
        Photo entity = this.photoRepository.findById(id).orElseThrow(() -> new PhotoNotFoundException(id));
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(rawBody);
            if (data.has("name")) {
                entity.setName(data.get("name").asString());
            }
            if (data.has("position")) {
                JsonNode positionNode = data.get("position");
                if (positionNode.isObject()) {
                    if (entity.getPosition() == null) {
                        entity.setPosition(new EmbeddedPosition());
                    }
                    if (positionNode.has("latitude")) {
                        entity.getPosition().setLatitude(positionNode.get("latitude").asDouble());
                    }
                    if (positionNode.has("longitude")) {
                        entity.getPosition().setLongitude(positionNode.get("longitude").asDouble());
                    }
                    if (positionNode.has("altitude")) {
                        entity.getPosition().setAltitude(positionNode.get("altitude").asDouble());
                    }
                    if (positionNode.has("timestamp")) {
                        entity.getPosition().setTimestamp(Instant.parse(positionNode.get("timestamp").asString()));
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        Photo saved = photoRepository.save(entity);
        PhotoDto dto = saved.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(saved, userDetails));
        return this.assembler.toModel(dto);
    }

    @GetMapping("{id}")
    EntityModel<PhotoDto> getEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Photo entity = this.photoRepository.findById(id).orElseThrow(() -> new PhotoNotFoundException(id));
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view photos.");
        }

        PhotoDto dto = entity.toDto();
        dto.setPermissions(this.permissionVerifier.getScopes(entity, userDetails));
        return this.assembler.toModel(dto);
    }

    @GetMapping("{id}/image")
    ResponseEntity<Resource> getEntityPhoto(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Photo entity = this.photoRepository.findById(id).orElseThrow(() -> new PhotoNotFoundException(id));
        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.VIEW, entity)) {
            throw new ForbiddenException("User does not have permission to view photos.");
        }

        try {
            Path path = Paths.get(pictureConfig.localPath() + "/" + entity.getPath());
            String mimeType = Files.probeContentType(path);
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok().header("Content-Type", mimeType).body(resource);
        } catch (Exception e) {
            log.error("Error loading photo: {}", "Not Found in Filesystem");

            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id, @AuthenticationPrincipal User userDetails) {
        Photo entity = this.photoRepository.findById(id).orElseThrow(() -> new PhotoNotFoundException(id));

        if (!this.permissionVerifier.hasAccess(userDetails, SecurityGroup.UserRoleScopeEnum.DELETE, entity)) {
            throw new ForbiddenException("User does not have permission to delete photos.");
        }
        try {
            Files.deleteIfExists(Path.of(pictureConfig.localPath() + "/" + entity.getPath()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        this.photoRepository.deleteById(id);
    }
}

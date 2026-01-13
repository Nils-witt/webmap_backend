package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.MapOverlayNotFoundException;
import dev.nilswitt.webmap.api.exceptions.UserNotFoundException;
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.repositories.MapOverlayRepository;
import dev.nilswitt.webmap.entities.repositories.MapOverlayRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
    CollectionModel<EntityModel<MapOverlay>> all() {
        List<EntityModel<MapOverlay>> entities = this.repository.findAll().stream()
                .map(this.assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(entities, linkTo(methodOn(MapOverlayController.class).all()).withSelfRel());
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<MapOverlay> newEntity(@RequestBody MapOverlay newEntity) {
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<MapOverlay> one(@PathVariable UUID id) {

        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new MapOverlayNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<MapOverlay> replaceEntity(@RequestBody MapOverlay newEntity, @PathVariable UUID id) {

        MapOverlay entity = this.repository.findById(id)
                .orElseThrow(() -> new MapOverlayNotFoundException(id));

        entity.setName(newEntity.getName());

        return this.assembler.toModel(this.repository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id) {
        this.repository.deleteById(id);
    }
}

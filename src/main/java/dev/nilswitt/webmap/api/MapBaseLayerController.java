package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.MapItemNotFoundException;
import dev.nilswitt.webmap.entities.MapBaseLayer;
import dev.nilswitt.webmap.entities.repositories.MapBaseLayerRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api/map/baselayers")
public class MapBaseLayerController {

    private final MapBaseLayerRepository repository;
    private final MapBaseLayerModelAssembler assembler;



    public MapBaseLayerController(MapBaseLayerRepository repository, MapBaseLayerModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<MapBaseLayer>> all() {
        List<EntityModel<MapBaseLayer>> users = this.repository.findAll().stream()
                .map(this.assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(MapBaseLayerController.class).all()).withSelfRel());
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<MapBaseLayer> newEntity(@RequestBody MapBaseLayer newEntity) {
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<MapBaseLayer> one(@PathVariable UUID id) {

        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new MapItemNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<MapBaseLayer> replaceEntity(@RequestBody MapBaseLayer newEntity, @PathVariable UUID id) {

        MapBaseLayer entity = this.repository.findById(id)
                .orElseThrow(() -> new MapItemNotFoundException(id));

        entity.setName(newEntity.getName());

        return this.assembler.toModel(this.repository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id) {
        this.repository.deleteById(id);
    }
}

package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.UnitNotFoundException;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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

    public UnitController(UnitRepository userRepository, UnitModelAssembler assembler) {
        this.repository = userRepository;
        this.assembler = assembler;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<Unit>> all() {
        List<EntityModel<Unit>> users = this.repository.findAll().stream()
                .map(this.assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(UnitController.class).all()).withSelfRel());
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<Unit> newEntity(@RequestBody Unit newEntity) {
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<Unit> one(@PathVariable UUID id) {

        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new UnitNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<Unit> replaceEntity(@RequestBody Unit newEntity, @PathVariable UUID id) {

        Unit entity = this.repository.findById(id)
                .orElseThrow(() -> new UnitNotFoundException(id));

        entity.setName(newEntity.getName());
        entity.setPosition(newEntity.getPosition());
        entity.setStatus(newEntity.getStatus());
        entity.setSpeakRequest(newEntity.isSpeakRequest());

        return this.assembler.toModel(this.repository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id) {
        this.repository.deleteById(id);
    }
}

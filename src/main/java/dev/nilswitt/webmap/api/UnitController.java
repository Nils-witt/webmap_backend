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

    private final UnitRepository unitRepository;
    private final UnitModelAssembler assembler;

    public UnitController(UnitRepository userRepository, UnitModelAssembler assembler) {
        this.unitRepository = userRepository;
        this.assembler = assembler;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<Unit>> all() {
        List<EntityModel<Unit>> users = this.unitRepository.findAll().stream()
                .map(this.assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(UnitController.class).all()).withSelfRel());
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<Unit> newEmployee(@RequestBody Unit newUnit) {
        return this.assembler.toModel(this.unitRepository.save(newUnit));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<Unit> one(@PathVariable UUID id) {

        return this.assembler.toModel(
                this.unitRepository.findById(id)
                        .orElseThrow(() -> new UnitNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<Unit> replaceUnit(@RequestBody Unit newEntity, @PathVariable UUID id) {

        Unit entity = this.unitRepository.findById(id)
                .orElseThrow(() -> new UnitNotFoundException(id));

        entity.setName(newEntity.getName());
        entity.setPosition(newEntity.getPosition());
        entity.setStatus(newEntity.getStatus());
        entity.setSpeakRequest(newEntity.isSpeakRequest());

        return this.assembler.toModel(this.unitRepository.save(entity));
    }

    @DeleteMapping("{id}")
    void deleteEmployee(@PathVariable UUID id) {
        this.unitRepository.deleteById(id);
    }
}

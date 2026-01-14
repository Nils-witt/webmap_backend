package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.UserNotFoundException;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserRepository repository;
    private final UserModelAssembler assembler;
    private final ApplicationEventPublisher eventPublisher;

    public UserController(UserRepository repository, UserModelAssembler assembler, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.assembler = assembler;
        this.eventPublisher = eventPublisher;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<User>> all() {
        List<EntityModel<User>> users = this.repository.findAll().stream()
                .map(this.assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<User> newEmployee(@RequestBody User newEntity) {
        return this.assembler.toModel(this.repository.save(newEntity));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<User> one(@PathVariable UUID id) {

        return this.assembler.toModel(
                this.repository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<User> replaceEntity(@RequestBody User newEntity, @PathVariable UUID id) {

        User entity = this.repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        entity.setUsername(newEntity.getUsername());
        entity.setEmail(newEntity.getEmail());
        entity.setFirstName(newEntity.getFirstName());
        entity.setLastName(newEntity.getLastName());
        User saved = this.repository.save(entity);
        //eventPublisher.publishEvent(new UserNameChangedEvent(saved.getId(), saved.getUsername(), saved.getFirstName(), saved.getLastName()));

        return this.assembler.toModel(saved);
    }

    @DeleteMapping("{id}")
    void deleteEntity(@PathVariable UUID id) {
        this.repository.deleteById(id);
    }
}

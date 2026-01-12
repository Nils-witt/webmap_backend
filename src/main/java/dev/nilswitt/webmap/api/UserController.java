package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.api.exceptions.UserNotFoundException;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
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

    private final UserRepository userRepository;
    private final UserModelAssembler assembler;

    public UserController(UserRepository userRepository, UserModelAssembler assembler) {
        this.userRepository = userRepository;
        this.assembler = assembler;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    CollectionModel<EntityModel<User>> all() {
        List<EntityModel<User>> users = this.userRepository.findAll().stream()
                .map(this.assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    EntityModel<User> newEmployee(@RequestBody User newEmployee) {
        return this.assembler.toModel(this.userRepository.save(newEmployee));
    }

    // Single item

    @GetMapping("{id}")
    EntityModel<User> one(@PathVariable UUID id) {

        return this.assembler.toModel(
                this.userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException(id))
        );
    }

    @PutMapping("{id}")
    EntityModel<User> replaceEmployee(@RequestBody User newUser, @PathVariable UUID id) {

        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setUsername(newUser.getUsername());
        user.setEmail(newUser.getEmail());
        user.setFirstName(newUser.getFirstName());
        user.setLastName(newUser.getLastName());

        return this.assembler.toModel(this.userRepository.save(user));
    }

    @DeleteMapping("{id}")
    void deleteEmployee(@PathVariable UUID id) {
        this.userRepository.deleteById(id);
    }
}

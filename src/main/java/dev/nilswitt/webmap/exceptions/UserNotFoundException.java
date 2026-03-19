package dev.nilswitt.webmap.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Could not find user " + id);
    }
}
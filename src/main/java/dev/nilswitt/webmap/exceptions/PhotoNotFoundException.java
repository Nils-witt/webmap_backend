package dev.nilswitt.webmap.exceptions;

import java.util.UUID;

public class PhotoNotFoundException extends RuntimeException {

    public PhotoNotFoundException(UUID id) {
        super("Could not find photo " + id);
    }
}
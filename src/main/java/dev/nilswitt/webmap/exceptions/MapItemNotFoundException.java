package dev.nilswitt.webmap.exceptions;

import java.util.UUID;

public class MapItemNotFoundException extends RuntimeException {

    public MapItemNotFoundException(UUID id) {
        super("Could not find item " + id);
    }
}
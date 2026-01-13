package dev.nilswitt.webmap.api.exceptions;

import java.util.UUID;

public class MapItemNotFoundException extends RuntimeException {

    public MapItemNotFoundException(UUID id) {
        super("Could not find item " + id);
    }
}
package dev.nilswitt.webmap.api.exceptions;

import java.util.UUID;

public class MapBaseLayerNotFoundException extends RuntimeException {
    public MapBaseLayerNotFoundException(UUID id) {
        super("Could not find item " + id);
    }
}

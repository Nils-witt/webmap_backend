package dev.nilswitt.webmap.exceptions;

import java.util.UUID;

public class MapOverlayNotFoundException extends RuntimeException {

    public MapOverlayNotFoundException(UUID id) {
        super("Could not find overlay " + id);
    }
}
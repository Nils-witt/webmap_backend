package dev.nilswitt.webmap.api.exceptions;

import java.util.UUID;

public class UnitNotFoundException extends RuntimeException {

    public UnitNotFoundException(UUID id) {
        super("Could not find unit " + id);
    }
}
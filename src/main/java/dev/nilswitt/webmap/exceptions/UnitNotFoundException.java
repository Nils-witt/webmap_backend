package dev.nilswitt.webmap.exceptions;

import java.util.UUID;

public class UnitNotFoundException extends RuntimeException {

    public UnitNotFoundException(UUID id) {
        super("Could not find unit " + id);
    }
}
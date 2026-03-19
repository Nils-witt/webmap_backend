package dev.nilswitt.webmap.exceptions;

import java.util.UUID;

public class MissionGroupNotFoundException extends RuntimeException {

    public MissionGroupNotFoundException(UUID id) {
        super("Could not find missionGroup " + id);
    }
}
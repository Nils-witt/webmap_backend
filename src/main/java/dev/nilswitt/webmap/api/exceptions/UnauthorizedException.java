package dev.nilswitt.webmap.api.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Not authorized to access resource ");
    }
}
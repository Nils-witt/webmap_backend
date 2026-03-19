package dev.nilswitt.webmap.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Not authorized to access resource ");
    }
}
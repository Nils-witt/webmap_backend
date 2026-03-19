package dev.nilswitt.webmap.api.rest.advices;

import dev.nilswitt.webmap.exceptions.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ForbiddenAdvice {

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String employeeNotFoundHandler(ForbiddenException ex) {
        return ex.getMessage();
    }
}
package dev.nilswitt.webmap.api.rest.advices;

import dev.nilswitt.webmap.exceptions.UnitNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class UnitNotFoundAdvice {

    @ExceptionHandler(UnitNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String employeeNotFoundHandler(UnitNotFoundException ex) {
        return ex.getMessage();
    }
}
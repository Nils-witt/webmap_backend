package dev.nilswitt.webmap.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class MapBaseLayerNotFoundAdvice {

    @ExceptionHandler(MapBaseLayerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String employeeNotFoundHandler(MapBaseLayerNotFoundException ex) {
        return ex.getMessage();
    }
}
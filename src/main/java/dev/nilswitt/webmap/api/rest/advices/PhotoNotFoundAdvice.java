package dev.nilswitt.webmap.api.rest.advices;

import dev.nilswitt.webmap.exceptions.PhotoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class PhotoNotFoundAdvice {

    @ExceptionHandler(PhotoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String photoNotFoundHandler(PhotoNotFoundException ex) {
        return ex.getMessage();
    }
}
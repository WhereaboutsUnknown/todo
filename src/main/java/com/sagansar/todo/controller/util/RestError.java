package com.sagansar.todo.controller.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
public class RestError {
    private final HttpStatus status;
    private final String error;
    private final List<String> errors;

    public RestError(HttpStatus status, String errorMessage, List<String> errors) {
        super();
        this.status = status;
        this.error = errorMessage;
        this.errors = errors;
    }

    public RestError(HttpStatus status, String errorMessage, String error) {
        super();
        this.status = status;
        this.error = errorMessage;
        errors = Collections.singletonList(error);
    }
}

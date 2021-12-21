package com.sagansar.todo.infrastructure.exceptions;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private boolean api;

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, boolean api) {
        super(message);
        this.api = api;
    }
}

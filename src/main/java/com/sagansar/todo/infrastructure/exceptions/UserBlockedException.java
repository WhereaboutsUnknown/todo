package com.sagansar.todo.infrastructure.exceptions;

import lombok.Getter;

@Getter
public class UserBlockedException extends RuntimeException {
    private final String responseMessage;

    public UserBlockedException(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}

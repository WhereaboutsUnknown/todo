package com.sagansar.todo.infrastructure.exceptions;

import lombok.Getter;

@Getter
public class BadRequestException extends Exception {
    private final String responseMessage;

    public BadRequestException(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}

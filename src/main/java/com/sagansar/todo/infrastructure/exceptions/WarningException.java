package com.sagansar.todo.infrastructure.exceptions;

import lombok.Getter;

@Getter
public class WarningException extends RuntimeException {
    String responseMessage;

    Object response;

    public WarningException(String responseMessage, Object response) {
        this.responseMessage = responseMessage;
        this.response = response;
    }
}

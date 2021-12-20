package com.sagansar.todo.infrastructure.exceptions;

import lombok.Getter;

@Getter
public class WarningException extends RuntimeException {
    private String responseMessage;

    private Object response;

    private Object addition;

    public WarningException(String responseMessage, Object response, Object addition) {
        this.responseMessage = responseMessage;
        this.response = response;
        this.addition = addition;
    }

    public WarningException(String responseMessage, Object response) {
        this.responseMessage = responseMessage;
        this.response = response;
    }

    public WarningException(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}

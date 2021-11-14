package com.sagansar.todo.infrastructure.exceptions;

import lombok.Getter;

@Getter
public class BadRequestException extends Exception {
    String responseMessage;

    public BadRequestException(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}

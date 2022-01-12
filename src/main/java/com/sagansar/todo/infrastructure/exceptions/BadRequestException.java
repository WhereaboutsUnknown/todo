package com.sagansar.todo.infrastructure.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestException extends Exception {
    private final String responseMessage;

    private String status;

    public BadRequestException(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public BadRequestException(String responseMessage, HttpStatus status) {
        this.responseMessage = responseMessage;
        this.status = String.valueOf(status.value());
    }
}

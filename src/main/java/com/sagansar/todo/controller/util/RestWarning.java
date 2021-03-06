package com.sagansar.todo.controller.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RestWarning {
    private final String warning;
    private Object response;
    private Object addition;

    public RestWarning(String warning) {
        this.warning = warning;
    }

    public RestWarning(String warning, Object response) {
        this.warning = warning;
        this.response = response;
    }

    public RestWarning(String warning, Object response, Object addition) {
        this.warning = warning;
        this.response = response;
        this.addition = addition;
    }

    public HttpStatus getStatus() {
        return HttpStatus.OK;
    }
}

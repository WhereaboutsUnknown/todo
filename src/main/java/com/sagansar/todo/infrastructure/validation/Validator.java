package com.sagansar.todo.infrastructure.validation;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;

public interface Validator {

    boolean validate(String value) throws BadRequestException;
}

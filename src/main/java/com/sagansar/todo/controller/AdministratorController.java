package com.sagansar.todo.controller;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdministratorController {

    @GetMapping("/test/1")
    public void test1() throws BadRequestException {
        throw new BadRequestException("BadRequestException thrown");
    }
}

package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ValidationServiceTest {

    @Autowired
    ValidationService validationService;

    @Test
    void test_real_id() {
        try {
            validationService.validateVk("id1");
        } catch (BadRequestException e) {
            throw new RuntimeException();
        }
    }

    @Test
    void test_public_id() {
        assertThrows(BadRequestException.class, () -> validationService.validateVk("public72495085"));
    }

    @Test
    void test_404() {
        assertThrows(BadRequestException.class, () -> validationService.validateVk("adfksfridjfgjddrgjfhghdjhrgsdhfjgd"));
    }

    @Test
    void test_deleted() {
        assertThrows(BadRequestException.class, () -> validationService.validateVk("id9426946"));
    }

    @Test
    void test_blocked() {
        assertThrows(BadRequestException.class, () -> validationService.validateVk("id1132"));
    }

    @Test
    void test_not_user() {
        assertThrows(BadRequestException.class, () -> validationService.validateVk("feed"));
    }
}
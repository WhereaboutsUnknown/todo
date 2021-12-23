package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.WorkerDto;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WorkerControllerTest {

    @Autowired
    WorkerController workerController;

    @Test
    void specification_filter() {
        List<String> criteria = List.of("name~Иван", "info~Spring");
        String op = "or";
        String by = "name";
        Pageable pageable = PageRequest.of(0, 1);
        try {
            List<WorkerDto> workers = workerController.findWorkers(criteria, op, by, "asc", pageable).getContent();
            assertNotNull(workers);
            assertFalse(workers.isEmpty());
        } catch (BadRequestException e) {
            throw new RuntimeException(e);
        }
    }

}
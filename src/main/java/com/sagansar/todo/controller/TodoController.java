package com.sagansar.todo.controller;

import com.sagansar.todo.repository.TodoTaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/todo", produces = MediaType.APPLICATION_JSON_VALUE)
public class TodoController {

    private final TodoTaskRepository todoTaskRepository;
}

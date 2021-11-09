package com.sagansar.todo.service;

import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.repository.TodoTaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TodoService {

    private TodoTaskRepository todoTaskRepository;

    public TodoService(TodoTaskRepository todoTaskRepository) {
        this.todoTaskRepository = todoTaskRepository;
    }

    public void createTask(Manager creator) {

    }
}

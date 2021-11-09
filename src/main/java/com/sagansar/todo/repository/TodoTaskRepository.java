package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.TodoTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoTaskRepository extends JpaRepository<TodoTask, Long> {
}

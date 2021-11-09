package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoStatusRepository extends JpaRepository<TodoStatus, Integer> {
}

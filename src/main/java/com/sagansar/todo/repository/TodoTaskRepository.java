package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.TodoTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoTaskRepository extends JpaRepository<TodoTask, Long> {
}

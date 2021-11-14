package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoStatusRepository extends JpaRepository<TodoStatus, Integer> {
}

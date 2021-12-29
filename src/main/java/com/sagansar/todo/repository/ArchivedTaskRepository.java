package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.ArchivedTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivedTaskRepository extends JpaRepository<ArchivedTask, Long> {
}

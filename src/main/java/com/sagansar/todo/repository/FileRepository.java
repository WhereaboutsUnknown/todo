package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.TaskFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<TaskFile, Long> {
}

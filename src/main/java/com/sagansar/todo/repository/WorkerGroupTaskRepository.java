package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.WorkerGroupTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerGroupTaskRepository extends JpaRepository<WorkerGroupTask, Long> {
}

package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.WorkerGroupTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerGroupTaskRepository extends JpaRepository<WorkerGroupTask, Long> {
    List<WorkerGroupTask> findAllByTaskId(Long taskId);
}

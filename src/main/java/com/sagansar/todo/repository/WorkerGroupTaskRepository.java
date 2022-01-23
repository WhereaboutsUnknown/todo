package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.WorkerGroupTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerGroupTaskRepository extends JpaRepository<WorkerGroupTask, Long> {
    void deleteAllByTaskId(Long taskId);

    List<WorkerGroupTask> findAllByTaskId(Long taskId);

    Optional<WorkerGroupTask> findByTaskIdAndResponsibleTrue(Long taskId);

    boolean existsByWorkerUserIdAndTaskId(Integer userId, Long taskId);
}

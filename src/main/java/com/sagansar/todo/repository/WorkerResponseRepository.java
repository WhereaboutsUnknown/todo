package com.sagansar.todo.repository;

import com.sagansar.todo.model.worker.WorkerResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerResponseRepository extends JpaRepository<WorkerResponse, Long> {

    List<WorkerResponse> findAllByTaskIdAndCheckedFalse(Long taskId);

    boolean existsByWorkerIdAndTaskId(Integer workerId, Long taskId);
}

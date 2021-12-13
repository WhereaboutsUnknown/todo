package com.sagansar.todo.repository;

import com.sagansar.todo.model.worker.WorkerResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerResponseRepository extends JpaRepository<WorkerResponse, Long> {
}
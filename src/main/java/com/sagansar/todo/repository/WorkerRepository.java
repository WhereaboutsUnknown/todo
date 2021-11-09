package com.sagansar.todo.repository;

import com.sagansar.todo.model.worker.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Integer> {
}

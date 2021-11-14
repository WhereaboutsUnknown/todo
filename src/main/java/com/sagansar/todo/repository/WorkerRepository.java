package com.sagansar.todo.repository;

import com.sagansar.todo.model.worker.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Integer> {

    Worker findByUserId(Integer userId);
}

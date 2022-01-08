package com.sagansar.todo.repository;

import com.sagansar.todo.model.worker.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Integer>, JpaSpecificationExecutor<Worker> {

    Optional<Worker> findByUserId(Integer userId);

    boolean existsByUserUsernameAndActiveTrue(String username);
}

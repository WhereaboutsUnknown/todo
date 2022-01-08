package com.sagansar.todo.repository;

import com.sagansar.todo.model.manager.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {

    Optional<Manager> findByUserId(Integer userId);

    boolean existsByUserUsernameAndActiveTrue(String username);
}

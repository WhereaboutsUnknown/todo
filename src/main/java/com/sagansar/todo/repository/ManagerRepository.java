package com.sagansar.todo.repository;

import com.sagansar.todo.model.manager.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<Manager, Integer> {
}

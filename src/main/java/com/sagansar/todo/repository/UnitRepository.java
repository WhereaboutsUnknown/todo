package com.sagansar.todo.repository;

import com.sagansar.todo.model.manager.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Integer> {
}

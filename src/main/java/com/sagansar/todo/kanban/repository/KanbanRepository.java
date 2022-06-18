package com.sagansar.todo.kanban.repository;

import com.sagansar.todo.kanban.model.Kanban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KanbanRepository extends JpaRepository<Kanban, Long> {

    Optional<Kanban> findByTaskId(Long taskId);
}

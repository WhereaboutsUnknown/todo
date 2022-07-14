package com.sagansar.todo.kanban.repository;

import com.sagansar.todo.kanban.model.KanbanColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {

    List<KanbanColumn> findAllByBoardTaskId(Long taskId);

    Optional<KanbanColumn> findByBoardTaskIdAndDeletableFalseAndFinishingFalse(Long taskId);

    @Query(value = "SELECT max(c.order) FROM kanban_column c WHERE c.board.id = :boardId")
    Integer maxOrder(Long boardId);

    Optional<KanbanColumn> findByBoardTaskIdAndId(Long taskId, Long id);

    Integer countAllByBoardTaskId(Long taskId);
}

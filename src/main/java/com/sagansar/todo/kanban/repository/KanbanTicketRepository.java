package com.sagansar.todo.kanban.repository;

import com.sagansar.todo.kanban.model.KanbanTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanbanTicketRepository extends JpaRepository<KanbanTicket, Long> {

    Optional<KanbanTicket> findByIdAndColumnBoardTaskId(Long id, Long taskId);

    List<KanbanTicket> findAllByColumnBoardId(Long boardId);

    List<KanbanTicket> findAllByColumnId(Long columnId);
}

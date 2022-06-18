package com.sagansar.todo.kanban.repository;

import com.sagansar.todo.kanban.model.KanbanComment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanCommentRepository extends JpaRepository<KanbanComment, Long> {

    List<KanbanComment> findAllByTicketId(Long ticketId);
}

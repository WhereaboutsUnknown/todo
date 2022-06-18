package com.sagansar.todo.kanban.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.kanban.model.KanbanColumn;
import com.sagansar.todo.kanban.model.KanbanTicket;
import com.sagansar.todo.kanban.repository.KanbanTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class KanbanService {

    private final KanbanTicketRepository kanbanTicketRepository;

    public KanbanTicket get(Long id, Long taskId) throws Exception {
        return kanbanTicketRepository.findByIdAndColumnBoardTaskId(id, taskId)
                .orElseThrow(() -> new BadRequestException("Тикет не найден"));
    }

    /**
     * Поместить тикет в столбец
     *
     * @param ticket тикет
     * @param column столбец
     */
    public void setColumn(KanbanTicket ticket, KanbanColumn column) throws Exception {
        if (!column.getBoard().getId().equals(ticket.getColumn().getBoard().getId())) {
            throw new BadRequestException("Столбец относится к другой доске");
        }
        if (ticket.getColumn().getFinishing() && !column.getFinishing()) {
            ticket.setFinishTime(null);
        } else if (!ticket.getColumn().getFinishing() && column.getFinishing()) {
            ticket.setFinishTime(LocalDateTime.now(ZoneId.systemDefault()));
        }
        ticket.setColumn(column);
        kanbanTicketRepository.save(ticket);
    }

    public KanbanTicket save(KanbanTicket ticket) {
        return kanbanTicketRepository.save(ticket);
    }

    public void delete(KanbanTicket ticket) {
        kanbanTicketRepository.delete(ticket);
    }

}

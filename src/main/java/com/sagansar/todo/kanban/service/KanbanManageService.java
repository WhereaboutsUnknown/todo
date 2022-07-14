package com.sagansar.todo.kanban.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.kanban.model.Kanban;
import com.sagansar.todo.kanban.model.KanbanColumn;
import com.sagansar.todo.kanban.model.KanbanTicket;
import com.sagansar.todo.kanban.repository.KanbanColumnRepository;
import com.sagansar.todo.kanban.repository.KanbanRepository;
import com.sagansar.todo.kanban.repository.KanbanTicketRepository;
import com.sagansar.todo.model.work.TodoTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KanbanManageService {

    private final KanbanColumnRepository kanbanColumnRepository;
    private final KanbanRepository kanbanRepository;
    private final KanbanTicketRepository kanbanTicketRepository;

    public KanbanColumn get(Long id, Long taskId) throws Exception {
        return kanbanColumnRepository.findByBoardTaskIdAndId(taskId, id)
                .orElseThrow(() -> new BadRequestException("Столбец не найден"));
    }

    public Integer getNextOrder(Kanban kanban) {
        var maxOrder = kanbanColumnRepository.maxOrder(kanban.getId());
        return Objects.requireNonNullElse(maxOrder, 0) + 1;
    }

    public Kanban getBoard(Long taskId) throws Exception {
        return kanbanRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BadRequestException("Доска не найдена"));
    }

    public void checkCapacity(Long taskId) throws BadRequestException {
        if (kanbanColumnRepository.countAllByBoardTaskId(taskId) >= 6) {
            throw new BadRequestException("Достигнут лимит столбцов на доске");
        }
    }

    public void attachBoard(TodoTask task) {
        var kanban = kanbanRepository.findByTaskId(task.getId()).orElse(new Kanban());
        kanban.setTask(task);
        kanbanRepository.save(kanban);

        var backlog = new KanbanColumn();
        backlog.setDeletable(false);
        backlog.setFinishing(false);
        backlog.setOrder(0);
        backlog.setName("Бэклог");
        backlog.setBoard(kanban);
        kanbanColumnRepository.save(backlog);

        var done = new KanbanColumn();
        done.setDeletable(false);
        done.setFinishing(true);
        done.setOrder(-1);
        done.setName("Сделано!");
        done.setBoard(kanban);
        kanbanColumnRepository.save(done);
    }

    public KanbanColumn save(KanbanColumn column) {
        return kanbanColumnRepository.save(column);
    }

    public List<KanbanTicket> delete(KanbanColumn column) throws Exception {
        if (!column.getDeletable()) {
            throw new BadRequestException("Столбец нельзя удалить");
        }
        var backlogContent = moveAllToBacklog(column);
        kanbanColumnRepository.delete(column);

        return backlogContent;
    }

    private List<KanbanTicket> moveAllToBacklog(KanbanColumn column) {
        var backlog = kanbanColumnRepository
                .findByBoardTaskIdAndDeletableFalseAndFinishingFalse(column.getBoard().getTask().getId())
                .orElseThrow(() -> new RuntimeException("Бэклог не найден"));
        var tickets = kanbanTicketRepository.findAllByColumnId(column.getId());
        tickets.forEach(t -> t.setColumn(backlog));
        kanbanTicketRepository.saveAll(tickets);
        return kanbanTicketRepository.findAllByColumnId(backlog.getId());
    }
}

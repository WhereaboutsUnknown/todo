package com.sagansar.todo.kanban;

import com.sagansar.todo.controller.dto.PersonNameDto;
import com.sagansar.todo.controller.mapper.Mapper;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.kanban.dto.*;
import com.sagansar.todo.kanban.model.KanbanComment;
import com.sagansar.todo.kanban.model.KanbanTicket;
import com.sagansar.todo.kanban.repository.KanbanColumnRepository;
import com.sagansar.todo.kanban.repository.KanbanCommentRepository;
import com.sagansar.todo.kanban.service.KanbanService;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.service.TodoService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/tasks/{taskId}/kanban/ticket", produces = MediaType.APPLICATION_JSON_VALUE)
public class KanbanController {

    private final KanbanService kanbanService;
    private final KanbanColumnRepository kanbanColumnRepository;
    private final KanbanCommentRepository kanbanCommentRepository;

    private final SecurityService securityService;
    private final TodoService todoService;

    @GetMapping
    public List<KanbanColumnRead> getColumns(
            @PathVariable Long taskId
    ) throws Exception {
        checkAccess(taskId);

        var columns = kanbanColumnRepository.findAllByBoardTaskId(taskId);
        return columns.stream()
                .map(col -> {
                    var columnView = new KanbanColumnRead();
                    Mapper.map(col, columnView);
                    var tickets = col.getTickets().stream()
                            .map(ticket -> {
                                var ticketView = new KanbanTicketFetch();
                                Mapper.map(ticket, ticketView);
                                var worker = PersonMapper.workerToName(ticket.getWorker());
                                ticketView.setWorker(worker);
                                return ticketView;
                            })
                            .collect(Collectors.toList());
                    columnView.setTickets(tickets);
                    return columnView;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public KanbanTicketRead read(
            @PathVariable Long taskId,
            @PathVariable Long id
    ) throws Exception {
        checkAccess(taskId);

        var ticket = kanbanService.get(id, taskId);

        var response = new KanbanTicketRead();
        Mapper.map(ticket, response);

        return response;
    }

    @PostMapping
    public KanbanTicketRead create(
            @PathVariable Long taskId,
            @RequestBody KanbanTicketEdit view
    ) throws Exception {
        checkAccess(taskId);

        var ticket = new KanbanTicket();
        var backlog = kanbanColumnRepository.findByBoardTaskIdAndDeletableFalseAndFinishingFalse(taskId)
                .orElseThrow(() -> new RuntimeException("Не найден столбец бэклога"));
        Mapper.map(view, ticket);
        kanbanService.setColumn(ticket, backlog);
        var response = new KanbanTicketRead();
        Mapper.map(ticket, response);

        return response;
    }

    @PutMapping("/{id}")
    public KanbanTicketRead update(
            @PathVariable Long taskId,
            @PathVariable Long id,
            @RequestBody KanbanTicketEdit view
    ) throws Exception {
        checkAccess(taskId);

        var ticket = kanbanService.get(id, taskId);
        Mapper.map(view, ticket);

        ticket = kanbanService.save(ticket);
        var response = new KanbanTicketRead();
        Mapper.map(ticket, response);

        return response;
    }

    @PutMapping("/{id}/drop")
    public void dropTicket(
            @PathVariable Long taskId,
            @PathVariable Long id,
            @RequestBody Long columnId
    ) throws Exception {
        checkAccess(taskId);

        if (columnId == null) {
            return;
        }
        var column = kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new BadRequestException("Столбец не найден"));
        var ticket = kanbanService.get(id, taskId);
        if (column.getId().equals(ticket.getColumn().getId())) {
            return;
        }
        kanbanService.setColumn(ticket, column);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long taskId,
            @PathVariable Long id
    ) throws Exception {
        checkAccess(taskId);

        var ticket = kanbanService.get(id, taskId);
        kanbanService.delete(ticket);
    }

    @GetMapping("/{id}/comment")
    public List<KanbanCommentRead> comments(
            @PathVariable Long taskId,
            @PathVariable Long id
    ) throws Exception {
        checkAccess(taskId);

        var ticket = kanbanService.get(id, taskId);
        var comments = kanbanCommentRepository.findAllByTicketId(ticket.getId());
        return comments.stream()
                .map(com -> {
                    var response = new KanbanCommentRead();
                    Mapper.map(com, response);
                    var author = new PersonNameDto();
                    Mapper.map(com.getAuthor(), author);
                    response.setAuthor(author);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/comment")
    public KanbanCommentRead comment(
            @PathVariable Long taskId,
            @PathVariable Long id,
            @RequestBody KanbanCommentPost view
    ) throws Exception {
        checkAccess(taskId);

        var ticket = kanbanService.get(id, taskId);
        var comment = new KanbanComment();
        Mapper.map(view, comment);
        comment.setAuthor(securityService.getCurrentUser());
        comment.setTicket(ticket);
        comment = kanbanCommentRepository.save(comment);
        var response = new KanbanCommentRead();
        Mapper.map(comment, response);

        return response;
    }

    private void checkAccess(Long taskId) throws Exception {
        todoService.checkUserRightsOnTaskAsWorker(securityService.getCurrentUser().getId(), taskId);
    }

}
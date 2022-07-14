package com.sagansar.todo.kanban;

import com.sagansar.todo.controller.dto.PersonNameDto;
import com.sagansar.todo.controller.mapper.Mapper;
import com.sagansar.todo.kanban.dto.KanbanColumnEdit;
import com.sagansar.todo.kanban.dto.KanbanColumnRead;
import com.sagansar.todo.kanban.dto.KanbanColumnShort;
import com.sagansar.todo.kanban.dto.KanbanTicketFetch;
import com.sagansar.todo.kanban.model.KanbanColumn;
import com.sagansar.todo.kanban.service.KanbanManageService;
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
@RequestMapping(path = "/tasks/{taskId}/kanban/manage", produces = MediaType.APPLICATION_JSON_VALUE)
public class KanbanManageController {

    private final KanbanManageService kanbanManageService;
    private final SecurityService securityService;
    private final TodoService todoService;

    @PostMapping("/col")
    public KanbanColumnRead create(
            @PathVariable Long taskId,
            @RequestBody KanbanColumnEdit view
    ) throws Exception {
        checkAccess(taskId);

        kanbanManageService.checkCapacity(taskId);
        var board = kanbanManageService.getBoard(taskId);
        var column = new KanbanColumn();
        Mapper.map(view, column);
        column.setBoard(board);
        column.setOrder(kanbanManageService.getNextOrder(board));
        column = kanbanManageService.save(column);
        var response = new KanbanColumnRead();
        Mapper.map(column, response);

        return response;
    }

    @PutMapping("/col/{id}")
    public KanbanColumnShort update(
            @PathVariable Long taskId,
            @PathVariable Long id,
            @RequestBody KanbanColumnEdit view
    ) throws Exception {
        checkAccess(taskId);

        var column = kanbanManageService.get(id, taskId);
        Mapper.map(view, column);
        column = kanbanManageService.save(column);
        var response = new KanbanColumnShort();
        Mapper.map(column, response);

        return response;
    }

    @DeleteMapping("/col/{id}")
    public List<KanbanTicketFetch> delete(
            @PathVariable Long taskId,
            @PathVariable Long id
    ) throws Exception {
        checkAccess(taskId);

        var column = kanbanManageService.get(id, taskId);
        var backlogContent = kanbanManageService.delete(column);
        return backlogContent.stream()
                .map(t -> {
                    var response = new KanbanTicketFetch();
                    Mapper.map(t, response);
                    var worker = new PersonNameDto();
                    Mapper.map(t.getWorker(), worker);
                    response.setWorker(worker);
                    return response;
                })
                .collect(Collectors.toList());
    }

    private void checkAccess(Long taskId) throws Exception {
        todoService.checkUserRightsOnTaskAsWorker(securityService.getCurrentUser().getId(), taskId);
    }
}

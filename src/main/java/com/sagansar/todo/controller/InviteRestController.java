package com.sagansar.todo.controller;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.service.InviteService;
import com.sagansar.todo.service.TodoService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/invite/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class InviteRestController {

    private final TodoService todoService;

    private final InviteService inviteService;

    @PostMapping ("")
    public Map<String, Object> processInviteAnswer(@RequestParam(name = "id") Long inviteId, @RequestParam(name = "accept") boolean accept) throws BadRequestException {
        Invite invite = inviteService.processInviteAnswer(inviteId, accept);
        TodoTask task = invite.getTask();
        if (task == null || statusInvalid(task)) {
            throw new BadRequestException("Задача недоступна!");
        }
        Worker worker = invite.getWorker();
        if (worker == null) {
            throw new BadRequestException("Приглашение не адресовано исполнителю!");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Ответ получен, " + (accept ? todoService.processAcceptedInvite(invite) : "Вы успешно отказались от задачи!"));
        //response.put("message", inviteId + ":" + accept);
        return response;
    }

    private boolean statusInvalid(TodoTask task) {
        TodoStatus status = task.getStatus();
        if (status == null) {
            return true;
        }
        TodoStatus.Status statusEnum = status.status();
        return !TodoStatus.Status.TODO.equals(statusEnum)
                && !TodoStatus.Status.DISCUSSION.equals(statusEnum)
                && !TodoStatus.Status.GO.equals(statusEnum);
    }
}

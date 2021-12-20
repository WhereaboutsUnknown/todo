package com.sagansar.todo.controller;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.service.InviteService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class AdministratorController {

    private final InviteService inviteService;

    private final TodoTaskRepository todoTaskRepository;

    @GetMapping("/test/1")
    public void test1() throws BadRequestException {
        throw new BadRequestException("BadRequestException thrown");
    }

    @GetMapping("/test/invite")
    public List<Integer> testInvite(@RequestParam(name = "taskId") Long taskId,
                                    @RequestParam(name = "workers")List<Integer> workers) throws BadRequestException {
        TodoTask task = todoTaskRepository.findById(taskId).orElseThrow(() -> new BadRequestException("Неверный id задачи!"));
        return inviteService.sendInvitesToAll(workers, task);
    }
}

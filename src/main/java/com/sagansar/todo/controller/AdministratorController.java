package com.sagansar.todo.controller;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.repository.UserRepository;
import com.sagansar.todo.service.InviteService;
import com.sagansar.todo.telegram.TelegramBotService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
@ResponseBody
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AdministratorController {

    private final InviteService inviteService;

    private final TodoTaskRepository todoTaskRepository;

    private final TelegramBotService telegramBotService;

    private final UserRepository userRepository;

    @GetMapping("/test/1")
    public void test1() throws BadRequestException {
        throw new BadRequestException("BadRequestException thrown");
    }

    @GetMapping("/test/httperr")
    public void test2(@RequestParam(name = "code") Integer code) {
        HttpStatus httpStatus = HttpStatus.resolve(code);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        throw new ResponseStatusException(httpStatus, "Успешно отловлена ошибка со статусом " + code);
    }

    @GetMapping("/test/invite")
    public List<Integer> testInvite(@RequestParam(name = "taskId") Long taskId,
                                    @RequestParam(name = "workers")List<Integer> workers) throws BadRequestException {
        TodoTask task = todoTaskRepository.findById(taskId).orElseThrow(() -> new BadRequestException("Неверный id задачи!"));
        return inviteService.sendInvitesToAll(workers, task);
    }

    @GetMapping("/test/url")
    public boolean sendUrlWithTg(@RequestParam(name = "id") Integer id, @RequestParam(name = "token") String token) throws BadRequestException {
        User user = userRepository.findById(id).orElseThrow(() -> new BadRequestException("Неверный id пользователя!"));
        return telegramBotService.sendMessage("Click on: ", user, "http://localhost:8080/invite?token=" + token);
    }
}

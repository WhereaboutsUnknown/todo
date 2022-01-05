package com.sagansar.todo.controller;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.UserRepository;
import com.sagansar.todo.telegram.TelegramBotService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class TestController {

    private final TelegramBotService telegramBotService;

    private final UserRepository userRepository;

    @GetMapping("/1")
    public void test1() throws BadRequestException {
        throw new BadRequestException("BadRequestException thrown");
    }

    @GetMapping("/httperr")
    public void test2(@RequestParam(name = "code") Integer code) {
        HttpStatus httpStatus = HttpStatus.resolve(code);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        throw new ResponseStatusException(httpStatus, "Успешно отловлена ошибка со статусом " + code);
    }

/*    @GetMapping("/invite")
    public List<Integer> testInvite(@RequestParam(name = "taskId") Long taskId,
                                    @RequestParam(name = "workers")List<Integer> workers) throws BadRequestException {
        TodoTask task = todoTaskRepository.findById(taskId).orElseThrow(() -> new BadRequestException("Неверный id задачи!"));
        return inviteService.sendInvitesToAll(workers, task);
    }*/

    @GetMapping("/url")
    public boolean sendUrlWithTg(@RequestParam(name = "id") Integer id, @RequestParam(name = "token") String token) throws BadRequestException {
        User user = userRepository.findById(id).orElseThrow(() -> new BadRequestException("Неверный id пользователя!"));
        return telegramBotService.sendMessage("Click on: ", user, "http://localhost:8080/invite?token=" + token);
    }
}

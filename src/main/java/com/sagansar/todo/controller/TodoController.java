package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.TaskBasic;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/todo", produces = MediaType.APPLICATION_JSON_VALUE)
public class TodoController {

    private final TodoTaskRepository todoTaskRepository;

    private final SecurityService securityService;

    @GetMapping("/{taskId}")
    public TaskBasic getTask(@PathVariable Long taskId) throws BadRequestException {
        if (!securityService.checkUserRights(RoleEnum.FREELANCER)) {
            throw new BadRequestException("Недостаточно прав для просмотра задачи!");
        }

        TodoTask task = todoTaskRepository.findById(taskId)
                .orElseThrow(() -> new BadRequestException("Задача не найдена!"));
        if (!task.is(TodoStatus.Status.TODO) && !task.is(TodoStatus.Status.DISCUSSION)) {
            throw new BadRequestException("Недостаточно прав для просмотра задачи!");
        }

        return TaskMapper.taskToBasic(task);
    }
}

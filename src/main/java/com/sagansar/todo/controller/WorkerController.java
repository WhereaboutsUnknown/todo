package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.controller.dto.WorkerDto;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.controller.mapper.WorkerMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.repository.WorkerRepository;
import com.sagansar.todo.service.DialogService;
import com.sagansar.todo.service.TodoService;
import com.sagansar.todo.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController("/worker")
public class WorkerController {

    @Autowired
    TodoTaskRepository todoTaskRepository;

    @Autowired
    WorkerRepository workerRepository;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    DialogService dialogService;

    @Autowired
    TodoService todoService;

    @Autowired

    @GetMapping("/")
    public WorkerDto getUserWorkerProfile() {
        User currentUser = userDetailsService.getCurrentUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Пользователь не найден");
        }
        Worker worker = workerRepository.findByUserId(currentUser.getId());
        if (worker == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Не найден профиль исполнителя");
        }
        if (!worker.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Профиль исполнителя был заблокирован");
        }
        return WorkerMapper.workerToDto(worker);
    }

    @GetMapping("/{workerId}/tasks")
    public List<TaskShortDto> getTakenTasks(@PathVariable(name = "workerId") Integer workerId) {
        checkWorkerRights(workerId);
        return todoTaskRepository.findAllByWorkerId(workerId).stream()
                .map(task -> {
                    TaskShortDto dto = TaskMapper.taskToShort(task);
                    dto.setPerson(PersonMapper.managerToName(task.getManager()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{workerId}/todo")
    public List<TaskShortDto> getAvailableTasks(@PathVariable(name = "workerId") Integer workerId) {
        if (!userDetailsService.checkUserRights(RoleEnum.FREELANCER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        return todoTaskRepository.findAllAvailable().stream()
                .map(task -> {
                    TaskShortDto dto = TaskMapper.taskToShort(task);
                    dto.setPerson(PersonMapper.managerToName(task.getManager()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/{workerId}/todo/{taskId}")
    public String claimTask(@PathVariable(name = "workerId") Integer workerId,
                          @PathVariable(name = "taskId") Long taskId,
                          @RequestParam(name = "message") String message) {
        Worker worker = checkWorkerRights(workerId);
        try {
            return todoService.claimTask(worker, taskId, message);
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getResponseMessage());
        }
    }

    private Worker checkWorkerRights(Integer workerId) {
        if (!userDetailsService.checkUserRights(RoleEnum.FREELANCER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        Worker worker = workerRepository.findById(workerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Исполнитель не найден"));
        User assignedUser = worker.getUser();
        User currentUser = userDetailsService.getCurrentUser();
        if (!Objects.equals(assignedUser.getUsername(), currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        if (!worker.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Профиль исполнителя был заблокирован");
        }
        return worker;
    }
}

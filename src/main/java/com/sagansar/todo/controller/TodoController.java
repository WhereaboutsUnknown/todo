package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.PersonNameDto;
import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.repository.WorkerRepository;
import com.sagansar.todo.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController("/todo")
public class TodoController {

    @Autowired
    TodoTaskRepository todoTaskRepository;

    @Autowired
    WorkerRepository workerRepository;

    @Autowired
    ManagerRepository managerRepository;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @GetMapping("/taken")
    public List<TaskShortDto> getTakenTasks(@RequestParam(name = "worker") Integer workerId) {
        checkWorkerRights(workerId);
        return todoTaskRepository.findAllByWorkerId(workerId).stream()
                .map(task -> {
                    Manager manager = managerRepository.findById(task.getManagerId()).orElse(null);
                    PersonNameDto personNameDto = null;
                    if (manager != null) {
                        personNameDto = PersonMapper.managerToName(manager);
                    }
                    return TaskMapper.taskToShort(task, personNameDto);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/")
    public List<TaskShortDto> getAvailableTasks(@RequestParam(name = "worker") Integer workerId) {
        checkWorkerRights(workerId);
        return todoTaskRepository.findAllAvailable().stream()
                .map(task -> {
                    Manager manager = managerRepository.findById(task.getManagerId()).orElse(null);
                    PersonNameDto personNameDto = null;
                    if (manager != null) {
                        personNameDto = PersonMapper.managerToName(manager);
                    }
                    return TaskMapper.taskToShort(task, personNameDto);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/managed")
    public List<TaskShortDto> getManagedTasks(@RequestParam(name = "manager") Integer managerId) {
        checkManagerRights(managerId);
        return todoTaskRepository.findAllByManagerId(managerId).stream()
                .map(task -> {
                    Worker worker = workerRepository.findById(task.getWorkerId()).orElse(null);
                    PersonNameDto personNameDto = null;
                    if (worker != null) {
                        personNameDto = PersonMapper.workerToName(worker);
                    }
                    return TaskMapper.taskToShort(task, personNameDto);
                })
                .collect(Collectors.toList());
    }

    private void checkWorkerRights(Integer workerId) {
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
    }

    private void checkManagerRights(Integer managerId) {
        if (!userDetailsService.checkUserRights(RoleEnum.MANAGER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        Manager manager = managerRepository.findById(managerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Менеджер не найден"));
        User assignedUser = manager.getUser();
        User currentUser = userDetailsService.getCurrentUser();
        if (!Objects.equals(assignedUser.getUsername(), currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        if (!manager.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Профиль менеджера был заблокирован");
        }
    }
}

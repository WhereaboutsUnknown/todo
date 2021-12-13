package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.ManagerDto;
import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.controller.mapper.ManagerMapper;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.external.TaskForm;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.service.TodoService;
import com.sagansar.todo.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Transactional
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    TodoTaskRepository todoTaskRepository;

    @Autowired
    ManagerRepository managerRepository;

    @Autowired
    SecurityService securityService;

    @Autowired
    ValidationService validationService;

    @Autowired
    TodoService todoService;

    @GetMapping("")
    public ManagerDto getUserManagerProfile() {
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Пользователь не найден");
        }
        Manager manager = managerRepository.findByUserId(currentUser.getId());
        if (manager == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Не найден профиль менеджера");
        }
        if (!manager.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Профиль менеджера был заблокирован");
        }
        return ManagerMapper.managerToDto(manager);
    }

    @GetMapping("/{managerId}")
    public ManagerDto getManager(@PathVariable(name = "managerId") Integer managerId) {
        return managerRepository.findById(managerId)
                .map(ManagerMapper::managerToDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не найден профиль менеджера"));
    }

    @GetMapping("/{managerId}/tasks")
    public List<TaskShortDto> getManagedTasks(@PathVariable(name = "managerId") Integer managerId) {
        checkManagerRights(managerId);
        return todoTaskRepository.findAllByManagerId(managerId).stream()
                .map(task -> {
                    TaskShortDto dto = TaskMapper.taskToShort(task);
                    dto.setPerson(PersonMapper.workerToName(task.getWorker()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //TODO: назначение менеджера на задачу, получение списка менеджеров (возможно, отдельный контроллер отделов)

    @PostMapping("/{managerId}/tasks")
    public TaskShortDto createTask(@PathVariable(name = "managerId") Integer managerId,
                                   @RequestBody TaskForm taskForm) throws BadRequestException {
        Manager manager = checkManagerRights(managerId);
        validationService.validate(taskForm);
        return TaskMapper.taskToShort(todoService.createTask(manager, taskForm));
    }

    private Manager checkManagerRights(Integer managerId) {
        if (!securityService.checkUserRights(RoleEnum.MANAGER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        Manager manager = managerRepository.findById(managerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Менеджер не найден"));
        User assignedUser = manager.getUser();
        User currentUser = securityService.getCurrentUser();
        if (!Objects.equals(assignedUser.getUsername(), currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        if (!manager.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Профиль менеджера был заблокирован");
        }
        return manager;
    }
}

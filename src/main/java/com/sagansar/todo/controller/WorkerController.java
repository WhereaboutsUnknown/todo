package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.controller.dto.WorkerDto;
import com.sagansar.todo.controller.dto.WorkerFullDto;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.controller.mapper.WorkerMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.external.WorkerProfileForm;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.repository.WorkerRepository;
import com.sagansar.todo.service.DialogService;
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
@RequestMapping("/worker")
public class WorkerController {

    @Autowired
    TodoTaskRepository todoTaskRepository;

    @Autowired
    WorkerRepository workerRepository;

    @Autowired
    SecurityService securityService;

    @Autowired
    DialogService dialogService;

    @Autowired
    ValidationService validationService;

    @Autowired
    TodoService todoService;

    @GetMapping("")
    public WorkerFullDto getUserWorkerProfile() {
        User currentUser = securityService.getCurrentUser();
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
        return WorkerMapper.workerToFullDto(worker);
    }

    @GetMapping("/{workerId}/tasks")
    public List<TaskShortDto> getTakenTasks(@PathVariable(name = "workerId") Integer workerId) {
        securityService.getAuthorizedWorker(workerId);
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
        if (!securityService.checkUserRights(RoleEnum.FREELANCER)) {
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
                          @RequestParam(name = "message") String message) throws BadRequestException {
        Worker worker = securityService.getAuthorizedWorker(workerId);
        return todoService.claimTask(worker, taskId, message);
    }

    @PostMapping("/{workerId}")
    public WorkerFullDto editProfile(@PathVariable(name = "workerId") Integer workerId,
                                     @RequestBody WorkerProfileForm workerProfileForm) throws BadRequestException {
        Worker worker = securityService.getAuthorizedWorker(workerId);
        Worker workerUpdate = WorkerMapper.fromWorkerProfileForm(workerProfileForm);
        validationService.validateVk(workerProfileForm.getVk());
        worker.copy(workerUpdate);

        return WorkerMapper.workerToFullDto(worker); //(workerRepository.save(worker));
    }

    @GetMapping("/{workerId}/profile")
    public WorkerFullDto getProfile(@PathVariable(name = "workerId") Integer workerId) {
        Worker worker = securityService.getAuthorizedWorker(workerId);
        return WorkerMapper.workerToFullDto(worker);
    }
}

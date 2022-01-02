package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.WorkerResponseBasic;
import com.sagansar.todo.controller.dto.WorkerResponseDto;
import com.sagansar.todo.controller.mapper.WorkerResponseMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.worker.WorkerResponse;
import com.sagansar.todo.repository.WorkerResponseRepository;
import com.sagansar.todo.service.NotificationService;
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
@RequestMapping(path = "/manager/{managerId}/responses", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskResponseController {

    private final WorkerResponseRepository workerResponseRepository;

    private final TodoService todoService;

    private final NotificationService notificationService;

    private final SecurityService securityService;

    @GetMapping("")
    public List<WorkerResponseBasic> getResponses(@PathVariable Integer managerId) {
        Manager manager = securityService.getAuthorizedManager(managerId);
        return manager.getTasks().stream()
                .map(TodoTask::getId)
                .flatMap(id -> workerResponseRepository.findAllByTaskIdAndAndCheckedFalse(id).stream())
                .map(WorkerResponseMapper::responseToBasic)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public WorkerResponseDto getResponse(@PathVariable Integer managerId, @PathVariable Long id) throws BadRequestException {
        securityService.getAuthorizedManager(managerId);
        return WorkerResponseMapper.responseToDto(
                getWorkerResponse(id)
        );
    }

    @PutMapping("/{id}")
    public WorkerResponseBasic acceptResponse(@PathVariable Integer managerId, @PathVariable Long id) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        WorkerResponse response = getWorkerResponse(id);
        if (response.getTask() == null || response.getWorker() == null) {
            throw new BadRequestException("Некорректный отклик!");
        }
        todoService.acceptWorkerForTask(manager, response.getTask(), response.getWorker());
        response.setChecked(true);
        return WorkerResponseMapper.responseToBasic(response);
    }

    @DeleteMapping("/{id}")
    public WorkerResponseBasic decline(@PathVariable Integer managerId, @PathVariable Long id) throws BadRequestException {
        securityService.getAuthorizedManager(managerId);
        WorkerResponse response = getWorkerResponse(id);
        if (response.getTask() == null || response.getWorker() == null) {
            throw new BadRequestException("Некорректный отклик!");
        }
        response.setChecked(true);
        notificationService.sendResponseDeclinedNotification(response.getWorker().getUser(), response.getTask().getHeader());
        return WorkerResponseMapper.responseToBasic(response);
    }

    //TODO: удалять старые уведомления и отклики

    public WorkerResponse getWorkerResponse(Long id) throws BadRequestException {
        return workerResponseRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Отклик не найден!"));
    }
}

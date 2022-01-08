package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.InviteDto;
import com.sagansar.todo.controller.dto.ManagerDto;
import com.sagansar.todo.controller.dto.TaskFullDto;
import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.controller.mapper.InviteMapper;
import com.sagansar.todo.controller.mapper.ManagerMapper;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.UserBlockedException;
import com.sagansar.todo.model.external.TaskEstimateTable;
import com.sagansar.todo.model.external.TaskForm;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.repository.WorkerRepository;
import com.sagansar.todo.service.*;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/manager", produces = MediaType.APPLICATION_JSON_VALUE)
public class ManagerController {

    private final TodoTaskRepository todoTaskRepository;

    private final ManagerRepository managerRepository;

    private final WorkerRepository workerRepository;

    private final SecurityService securityService;

    private final ValidationService validationService;

    private final TodoService todoService;

    private final InviteService inviteService;

    private final ArchiveService archiveService;

    @GetMapping("")
    public ManagerDto getUserManagerProfile() {
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null) {
            throw new UserBlockedException("Пользователь не найден");
        }
        Manager manager = managerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UserBlockedException("Профиль менеджера не создан или был удален"));
        if (!manager.isActive()) {
            throw new UserBlockedException("Профиль менеджера не активен");
        }
        return ManagerMapper.managerToDto(manager);
    }

    @GetMapping("/{managerId}")
    public ManagerDto getManager(@PathVariable(name = "managerId") Integer managerId) throws BadRequestException {
        validationService.checkNullSafety(managerId);
        return managerRepository.findById(managerId)
                .map(ManagerMapper::managerToDto)
                .orElseThrow(() -> new BadRequestException("Профиль менеджера не найден!"));
    }

    @GetMapping("/{managerId}/tasks")
    public List<TaskShortDto> getManagedTasks(@PathVariable(name = "managerId") Integer managerId,
                                              @RequestParam(name = "unit", required = false) boolean needUnit) {
        Manager manager = securityService.getAuthorizedManager(managerId);
        List<TodoTask> tasks;
        if (needUnit && securityService.checkUserRights(RoleEnum.SUPERVISOR)) {
            tasks = todoTaskRepository.findAllByUnitId(manager.getUnit().getId());
        } else {
            tasks = todoTaskRepository.findAllByManagerId(managerId);
        }
        return tasks.stream()
                .map(task -> {
                    TaskShortDto dto = TaskMapper.taskToShort(task);
                    dto.setPerson(PersonMapper.workerToName(task.getWorker()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/{managerId}/tasks")
    public TaskShortDto createTask(@PathVariable(name = "managerId") Integer managerId,
                                   @RequestBody TaskForm taskForm) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        validationService.validate(taskForm);
        return TaskMapper.taskToShort(todoService.createTask(manager, taskForm));
    }

    @GetMapping("/{managerId}/tasks/{taskId}")
    public TaskFullDto getTask(@PathVariable(name = "managerId") Integer managerId,
                               @PathVariable(name = "taskId") Long taskId) throws BadRequestException {
        securityService.getAuthorizedManager(managerId);
        if (taskId == null) {
            throw new BadRequestException("В запросе отсутствует ID задачи!");
        }
        return TaskMapper.taskToFull(todoTaskRepository.findById(taskId)
                .orElseThrow(() -> new BadRequestException("Задача не найдена!")));
    }

    @PostMapping("/{managerId}/tasks/{taskId}")
    public TaskFullDto publishTask(@PathVariable(name = "managerId") Integer managerId,
                                   @PathVariable(name = "taskId") Long taskId,
                                   @RequestParam(name = "all", required = false) boolean visibleToAll,
                                   @RequestBody List<Integer> workersToInvite) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        if (taskId == null) {
            throw new BadRequestException("В запросе отсутствует ID задачи!");
        }
        TodoTask task = todoService.publishTask(manager, taskId, visibleToAll);
        TaskFullDto dto = TaskMapper.taskToFull(task);
        List<Integer> invited = inviteService.sendInvitesToAll(workersToInvite, task);
        dto.setInvited(invited);

        return dto;
    }

    @PostMapping("/{managerId}/tasks/{taskId}/archive")
    public TaskFullDto archiveTask(@PathVariable(name = "managerId") Integer managerId,
                                   @PathVariable(name = "taskId") Long taskId,
                                   @RequestBody List<TaskEstimateTable> estimateTables) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        validationService.checkNullSafety(manager, taskId, estimateTables);
        TodoTask task = todoService.getTaskForArchiving(manager, taskId);
        if (manager.getUnit() == null) {
            throw new BadRequestException("Менеджер не относится ни к одному отделу!");
        }
        Unit unit = manager.getUnit();
        if (!unit.getId().equals(task.getUnit().getId())) {
            throw new BadRequestException("Задача не относится к отделу " + unit.getName());
        }
        Manager creator = task.getCreator();
        return TaskMapper.taskToFull(archiveService.archiveTask(manager, task, creator, unit, estimateTables));
    }

    @GetMapping("/{managerId}/tasks/{taskId}/invites")
    public List<InviteDto> getTaskInvites(@PathVariable(name = "managerId") Integer managerId,
                                          @PathVariable(name = "taskId") Long taskId) {
        securityService.getAuthorizedManager(managerId);
        return inviteService.findInvitesOnTask(taskId).stream()
                .filter(invite -> managerId.equals(invite.getTask().getManager().getId()))
                .map(InviteMapper::inviteToDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{managerId}/tasks/{taskId}")
    public TaskFullDto reviewTask(@PathVariable(name = "managerId") Integer managerId,
                                  @PathVariable(name = "taskId") Long taskId,
                                  @RequestBody Boolean review) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        validationService.checkNullSafety(taskId);
        return TaskMapper.taskToFull(todoService.review(manager, taskId, review));
    }

    @DeleteMapping("/{managerId}/tasks/{taskId}")
    public TaskFullDto cancelTask(@PathVariable(name = "managerId") Integer managerId,
                                  @PathVariable(name = "taskId") Long taskId) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        validationService.checkNullSafety(taskId);
        return TaskMapper.taskToFull(todoService.cancel(manager, taskId));
    }

    @PutMapping("/{managerId}/tasks/{taskId}/worker")
    public TaskFullDto setResponsibleWorker(@PathVariable(name = "managerId") Integer managerId,
                                  @PathVariable(name = "taskId") Long taskId,
                                  @RequestParam(name = "id") Integer workerId) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        validationService.checkNullSafety(taskId, workerId);
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new BadRequestException("Работник не найден!"));
        return TaskMapper.taskToFull(todoService.setWorkerResponsible(manager, taskId, worker));
    }

    @DeleteMapping("/{managerId}/tasks/{taskId}/worker")
    public TaskFullDto removeWorker(@PathVariable(name = "managerId") Integer managerId,
                                  @PathVariable(name = "taskId") Long taskId,
                                  @RequestParam(name = "id") Integer workerId) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        validationService.checkNullSafety(taskId, workerId);
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new BadRequestException("Работник не найден!"));
        return TaskMapper.taskToFull(todoService.deleteWorkerFromTask(manager, worker, taskId));
    }

    @PreAuthorize("hasAuthority('SUPERVISOR')")
    @PutMapping("/{supervisorId}/tasks/{taskId}/manager")
    public TaskFullDto setTaskManager(@PathVariable(name = "supervisorId") Integer supervisorId,
                                            @PathVariable(name = "taskId") Long taskId,
                                            @RequestParam(name = "id") Integer managerId) throws BadRequestException {
        securityService.getAuthorizedManager(supervisorId);
        validationService.checkNullSafety(taskId, managerId);
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new BadRequestException("Менеджер не найден!"));
        return TaskMapper.taskToFull(todoService.setTaskManager(manager, taskId));
    }

    @PreAuthorize("hasAuthority('SUPERVISOR')")
    @DeleteMapping("/{supervisorId}/tasks/{taskId}/manager")
    public TaskFullDto removeTaskManager(@PathVariable(name = "supervisorId") Integer supervisorId,
                                    @PathVariable(name = "taskId") Long taskId) throws BadRequestException {
        securityService.getAuthorizedManager(supervisorId);
        validationService.checkNullSafety(taskId);
        return TaskMapper.taskToFull(todoService.removeTaskManager(taskId));
    }
}

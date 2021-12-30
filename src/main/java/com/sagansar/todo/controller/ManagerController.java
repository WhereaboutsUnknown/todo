package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.ManagerDto;
import com.sagansar.todo.controller.dto.TaskFullDto;
import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.controller.mapper.ManagerMapper;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.external.TaskEstimateTable;
import com.sagansar.todo.model.external.TaskForm;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.service.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    private final SecurityService securityService;

    private final ValidationService validationService;

    private final TodoService todoService;

    private final InviteService inviteService;

    private final ArchiveService archiveService;

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
        securityService.getAuthorizedManager(managerId);
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
        Manager manager = securityService.getAuthorizedManager(managerId);
        validationService.validate(taskForm);
        return TaskMapper.taskToShort(todoService.createTask(manager, taskForm));
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

        return dto; //TODO возможно, нужно сперва получать список программистов, отсортированных по подходящим скиллам, выбирать из них и отправлять массив, для кого видна
    }

    @PostMapping("/{managerId}/tasks/{taskId}/archive")
    public TaskFullDto archiveTask(@PathVariable(name = "managerId") Integer managerId,
                                   @PathVariable(name = "taskId") Long taskId,
                                   @RequestBody List<TaskEstimateTable> estimateTables) throws BadRequestException {
        Manager manager = securityService.getAuthorizedManager(managerId);
        if (taskId == null) {
            throw new BadRequestException("В запросе отсутствует ID задачи!");
        }
        TodoTask task = todoService.getTaskForArchiving(manager, taskId);
        if (manager.getUnit() == null) {
            throw new BadRequestException("Менеджер не относится ни к одному отделу!");
        }
        Unit unit = manager.getUnit();
        if (!unit.getId().equals(task.getUnitId())) {
            throw new BadRequestException("Задача не относится к отделу " + unit.getName());
        }
        Manager creator = managerRepository.findById(task.getCreatorId())
                .orElseThrow(() -> new BadRequestException("У задачи не заполнено поле создателя!"));
        return TaskMapper.taskToFull(archiveService.archiveTask(manager, task, creator, unit, estimateTables));
    }
}

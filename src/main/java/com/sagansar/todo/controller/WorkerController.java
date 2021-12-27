package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.controller.dto.WorkerDto;
import com.sagansar.todo.controller.dto.WorkerFullDto;
import com.sagansar.todo.controller.dto.WorkerLineDto;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.controller.mapper.WorkerMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.UnauthorizedException;
import com.sagansar.todo.infrastructure.sort.WorkerSkillSorter;
import com.sagansar.todo.infrastructure.specifications.FilterCompiler;
import com.sagansar.todo.infrastructure.specifications.SearchCriteria;
import com.sagansar.todo.model.external.WorkerProfileForm;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.repository.WorkerRepository;
import com.sagansar.todo.service.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/worker", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkerController {

    private final TodoTaskRepository todoTaskRepository;

    private final WorkerRepository workerRepository;

    private final SecurityService securityService;

    private final DialogService dialogService;

    private final ValidationService validationService;

    private final TodoService todoService;

    private final InviteService inviteService;

    private final FilterCompiler<Worker> filterCompiler;

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
    public TaskShortDto claimTask(@PathVariable(name = "workerId") Integer workerId,
                          @PathVariable(name = "taskId") Long taskId,
                          @RequestParam(name = "message") String message) throws BadRequestException {
        Worker worker = securityService.getAuthorizedWorker(workerId);
        return TaskMapper.taskToShort(todoService.claimTask(worker, taskId, message));
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

    @GetMapping("/search")
    public Page<WorkerDto> findWorkers(@RequestParam(name = "crit", required = false) List<String> criteria,
                                    @RequestParam(name = "op", required = false) String operator,
                                    @RequestParam(name = "by", required = false) String sort,
                                    @RequestParam(name = "dir", required = false) String direction,
                                    Pageable pageable) throws BadRequestException {
        if (!securityService.checkUserRights(RoleEnum.MANAGER) && !securityService.isAdmin()) {
            throw new UnauthorizedException("Недостаточно полномочий для доступа", true);
        }
        if (sort != null && direction != null) {
            pageable = validationService.validatePageRequest(pageable, sort, direction);
        }
        Specification<Worker> filter = null;
        if (criteria != null && operator != null) {
            filter = filterCompiler.compile(
                    criteria.stream()
                    .map(filterCompiler::compile)
                    .filter(crit -> crit.getKey() != null && crit.getOperation() != null && crit.getValue() != null)
                    .collect(Collectors.toList()),
                    operator
            );
            filter = filterCompiler.append(filter, new SearchCriteria("active", ":", "true"), FilterCompiler.Operator.AND);
        }
        return workerRepository.findAll(filter, pageable)
                .map(WorkerMapper::workerToDto);
    }

    @GetMapping("/search/for")
    public List<WorkerLineDto> findWorkersForTask(@RequestParam(name = "task") Long taskId,
                                                  Pageable pageable) throws BadRequestException {
        if (!securityService.checkUserRights(RoleEnum.MANAGER) && !securityService.isAdmin()) {
            throw new UnauthorizedException("Недостаточно полномочий для доступа", true);
        }
        TodoTask task = todoTaskRepository.findById(taskId).orElseThrow(() -> new BadRequestException("Отсутствует задача!"));
        String[] skills = task.getStack().replace(", ", ",").split(",");
        Specification<Worker> filter = filterCompiler.compile(
                Stream.of(skills)
                .map(s -> new SearchCriteria("info", "~", s))
                .collect(Collectors.toList()),
                "or");

        List<Worker> workers = workerRepository.findAll(filter, pageable).getContent();
        return WorkerSkillSorter.sort(workers, skills).stream()
                .map(WorkerMapper::workerToLine)
                .collect(Collectors.toList());
    }
}

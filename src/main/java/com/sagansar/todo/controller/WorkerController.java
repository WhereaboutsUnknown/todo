package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.*;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.controller.mapper.WorkerMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.UnauthorizedException;
import com.sagansar.todo.infrastructure.exceptions.UserBlockedException;
import com.sagansar.todo.infrastructure.sort.WorkerSkillSorter;
import com.sagansar.todo.infrastructure.specifications.FilterCompiler;
import com.sagansar.todo.infrastructure.specifications.SearchCriteria;
import com.sagansar.todo.infrastructure.specifications.SearchSpecification;
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
import org.springframework.util.StringUtils;
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
            throw new UserBlockedException("Пользователь не найден");
        }
        Worker worker = workerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UserBlockedException("Профиль исполнителя не создан или был удален"));
        if (!worker.isActive()) {
            throw new UserBlockedException("Профиль исполнителя не активен");
        }
        return WorkerMapper.workerToFullDto(worker);
    }

    @GetMapping("/{workerId}")
    public WorkerDto getWorker(@PathVariable Integer workerId) throws BadRequestException {
        securityService.checkUserRights(RoleEnum.MANAGER);
        validationService.checkNullSafety(workerId);
        return workerRepository.findById(workerId)
                .map(WorkerMapper::workerToDto)
                .orElseThrow(() -> new BadRequestException("Профиль сотрудника не найден!"));
    }

    @GetMapping("/{workerId}/tasks")
    public List<TaskShortDto> getTakenTasks(@PathVariable(name = "workerId") Integer workerId) {
        securityService.getAuthorizedWorker(workerId);
        return todoTaskRepository.findAllByWorkerId(workerId).stream()
                .map(TaskMapper::taskToShort)
                .collect(Collectors.toList());
    }

    @GetMapping("/{workerId}/todo")
    public List<TaskShortDto> getAvailableTasks(@PathVariable(name = "workerId") Integer workerId) {
        if (!securityService.checkUserRights(RoleEnum.FREELANCER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        return todoTaskRepository.findAllAvailable().stream()
                .map(TaskMapper::taskToShort)
                .collect(Collectors.toList());
    }

    @PostMapping("/{workerId}/todo/{taskId}")
    public TaskFullDto claimTask(@PathVariable(name = "workerId") Integer workerId,
                          @PathVariable(name = "taskId") Long taskId,
                          @RequestParam(name = "message") String message) throws BadRequestException {
        Worker worker = securityService.getAuthorizedWorker(workerId);
        return TaskMapper.taskToFull(todoService.claimTask(worker, taskId, message));
    }

    @PutMapping("/{workerId}/todo/{taskId}")
    public TaskFullDto taskDone(@PathVariable(name = "workerId") Integer workerId,
                                @PathVariable(name = "taskId") Long taskId) throws BadRequestException {
        Worker worker = securityService.getAuthorizedWorker(workerId);
        return TaskMapper.taskToFull(todoService.done(worker, taskId));
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
                                    @RequestParam(name = "req", required = false) String request,
                                    Pageable pageable) throws BadRequestException {
        if (!securityService.checkUserRights(RoleEnum.MANAGER) && !securityService.isAdmin()) {
            throw new UnauthorizedException("Недостаточно полномочий для доступа", true);
        }
        if (sort != null && direction != null) {
            pageable = validationService.validatePageRequest(pageable, sort, direction);
        }
        Specification<Worker> filter = new SearchSpecification<>(new SearchCriteria("active", ":", "true"));
        if (criteria != null && operator != null) {
            Specification<Worker> criteriaFilter = filterCompiler.compile(
                    criteria.stream()
                    .map(filterCompiler::compile)
                    .filter(crit -> crit.getKey() != null && crit.getOperation() != null && crit.getValue() != null)
                    .collect(Collectors.toList()),
                    operator
            );
            filter = filter.and(criteriaFilter);
        }
        if (StringUtils.hasText(request)) {
            Specification<Worker> requestFilter = new SearchSpecification<>(new SearchCriteria("info", "~", request));
            requestFilter = filterCompiler.append(requestFilter, new SearchCriteria("name", "~", request), FilterCompiler.Operator.OR);
            filter = filter.and(requestFilter);
        }
        return workerRepository.findAll(filter, pageable)
                .map(WorkerMapper::workerToDto);
    }

    @GetMapping("/search/for")
    public List<WorkerLineDto> findWorkersForTask(@RequestParam(name = "task") Long taskId,
                                                  @RequestParam(name = "show", required = false) String show,
                                                  Pageable pageable) throws BadRequestException {
        if (!securityService.checkUserRights(RoleEnum.MANAGER) && !securityService.isAdmin()) {
            throw new UnauthorizedException("Недостаточно полномочий для доступа", true);
        }
        TodoTask task = todoTaskRepository.findById(taskId).orElseThrow(() -> new BadRequestException("Отсутствует задача!"));
        String[] skills = task.getStack().replace(", ", ",").split(",");
        if ("all".equals(show)) {
            return WorkerSkillSorter.sort(workerRepository.findAll(pageable).getContent(), skills).stream()
                    .map(WorkerMapper::workerToLine)
                    .collect(Collectors.toList());
        }
        Specification<Worker> filter = filterCompiler.compile(
                Stream.of(skills)
                .map(s -> new SearchCriteria("info", "~", s))
                .collect(Collectors.toList()),
                "or");
        filter = filterCompiler.append(filter, new SearchCriteria("active", ":", "true"), FilterCompiler.Operator.AND);

        List<Worker> workers = workerRepository.findAll(filter, pageable).getContent();
        return WorkerSkillSorter.sort(workers, skills).stream()
                .map(WorkerMapper::workerToLine)
                .collect(Collectors.toList());
    }
}

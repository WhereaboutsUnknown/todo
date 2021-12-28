package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.external.TaskForm;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.work.WorkerGroupTask;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.model.worker.WorkerResponse;
import com.sagansar.todo.repository.TodoStatusRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.repository.WorkerGroupTaskRepository;
import com.sagansar.todo.repository.WorkerResponseRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class TodoService {

    private final TodoTaskRepository todoTaskRepository;

    private final WorkerGroupTaskRepository workerGroupTaskRepository;

    private final TodoStatusRepository todoStatusRepository;

    private final DialogService dialogService;

    private final NotificationService notificationService;

    private final WorkerResponseRepository responseRepository;

    public TodoService(TodoTaskRepository todoTaskRepository,
                       WorkerGroupTaskRepository workerGroupTaskRepository,
                       TodoStatusRepository todoStatusRepository,
                       NotificationService notificationService,
                       DialogService dialogService,
                       WorkerResponseRepository responseRepository) {
        this.workerGroupTaskRepository = workerGroupTaskRepository;
        this.todoStatusRepository = todoStatusRepository;
        this.todoTaskRepository = todoTaskRepository;
        this.notificationService = notificationService;
        this.dialogService = dialogService;
        this.responseRepository = responseRepository;
    }

    /**
     * Create new task draft from valid form
     *
     * @param creator manager profile that is an applier of current form
     * @param taskForm task form submitted
     * @return created task
     */
    public TodoTask createTask(@NonNull Manager creator, @NonNull TaskForm taskForm) {
        return newTask(creator, taskForm);
    }

    /**
     * Publish created task
     *
     * @param manager manager of current task
     * @param taskId task ID
     * @param visibleToAll false if only invited Workers see this task, true if everyone does
     * @return published task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask publishTask(@NonNull Manager manager, @NonNull Long taskId, boolean visibleToAll) throws BadRequestException {
        TodoTask task = getValidTaskForPublishing(taskId);
        checkManagerRightsOnTask(manager, task);
        task.setStatus(getStatus(TodoStatus.Status.TODO));
        task.setVisibleToAll(visibleToAll);

        return todoTaskRepository.save(task);
    }

    /**
     * Perform claim step on task (from worker searching to discussion)
     *
     * @param worker claiming worker
     * @param taskId task ID
     * @param message message from worker for task manager
     * @return success message
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask claimTask(@NonNull Worker worker, @NonNull Long taskId, String message) throws BadRequestException {
        TodoTask task = getValidTaskForClaim(taskId);
        if (!task.isVisibleToAll()) {
            throw new BadRequestException("Стать исполнителем для этой задачи можно только по приглашению!");
        }
        sendWorkerResponse(task, worker, message);
        dialogService.createDialog(task, worker.getUser(), message);

        if (!TodoStatus.Status.DISCUSSION.equals(task.getStatus().status())) {
            TodoStatus discussion = getStatus(TodoStatus.Status.DISCUSSION);
            task.setStatus(discussion);
            todoTaskRepository.save(task);
        }
        notificationService.sendTaskClaimNotification(task.getManager().getUser(), task.getHeader(), worker.getName());

        return task;
    }

    /**
     * Add Worker to task group, set as responsible if no one responded yet
     *
     * @param task task
     * @param worker Worker
     * @return success message
     */
    public String addWorker(@NonNull TodoTask task, @NonNull Worker worker) {
        Optional<WorkerGroupTask> link = workerGroupTaskRepository.findById(generateCompositeId(task.getId(), worker.getId()));
        if (link.isEmpty()) {
            WorkerGroupTask workerGroupTask = new WorkerGroupTask();
            workerGroupTask.setWorker(worker);
            workerGroupTask.setTask(task);
            if (task.getWorker() == null) {
                task.setWorker(worker);
                workerGroupTask.setResponsible(true);
            }
            workerGroupTaskRepository.save(workerGroupTask);
            if (!TodoStatus.Status.GO.equals(task.getStatus().status())) {
                TodoStatus go = todoStatusRepository.getById(TodoStatus.Status.GO.getCode());
                task.setStatus(go);
                todoTaskRepository.save(task);
            }
        }
        return "задача успешно поручена Вам!";
    }

    /**
     * Set Worker as responsible for task
     *
     * @param task task
     * @param worker Worker
     * @return saved task
     * @throws BadRequestException if worker is not associated with this task
     */
    public TodoTask setWorkerResponsible(@NonNull TodoTask task, @NonNull Worker worker) throws BadRequestException {
        WorkerGroupTask link = workerGroupTaskRepository.findById(generateCompositeId(task.getId(), worker.getId()))
                .orElseThrow(() -> new BadRequestException("Работник не является исполнителем данной задачи!"));
        link.setResponsible(true);
        task.setWorker(worker);
        workerGroupTaskRepository.save(link);
        return todoTaskRepository.save(task);
    }

    /**
     * Validate TodoTask for claim step and get valid one
     *
     * @param taskId task ID
     * @return valid TodoTask
     * @throws BadRequestException in case of invalid task ID
     */
    private TodoTask getValidTaskForClaim(@NonNull Long taskId) throws BadRequestException {
        TodoTask task = getValidTask(taskId);
        TodoStatus.Status statusEnum = task.getStatus().status();
        if (!TodoStatus.Status.DISCUSSION.equals(statusEnum) && !TodoStatus.Status.TODO.equals(statusEnum)) {
            throw new BadRequestException("Задача [" + taskId + "] имеет некорректный статус: " + statusEnum);
        }
        return task;
    }

    /**
     * Validate TodoTask for publishing step and get valid one
     *
     * @param taskId task ID
     * @return valid TodoTask
     * @throws BadRequestException in case of invalid task ID
     */
    private TodoTask getValidTaskForPublishing(@NonNull Long taskId) throws BadRequestException {
        TodoTask task = getValidTask(taskId);
        TodoStatus.Status statusEnum = task.getStatus().status();
        if (TodoStatus.Status.DRAFT.equals(statusEnum)) {
            throw new BadRequestException("Задача уже была опубликована!");
        }
        return task;
    }

    /**
     * Validate TodoTask for existence and having status and get valid one
     *
     * @param taskId task ID
     * @return existing TodoTask with status
     * @throws BadRequestException in case of no such task or task having no status
     */
    private TodoTask getValidTask(@NonNull Long taskId) throws BadRequestException {
        TodoTask task = todoTaskRepository.findById(taskId)
                .orElseThrow(() -> new BadRequestException("Задача " + taskId + " не найдена"));
        TodoStatus status = task.getStatus();
        if (status == null) {
            throw new BadRequestException("У задачи [" + taskId + "] отсутствует статус!");
        }
        return task;
    }

    /**
     * Create new task
     *
     * @param creator manager profile that is an applier of current form
     * @param taskForm task form submitted
     * @return created task
     */
    private TodoTask newTask(@NonNull Manager creator, @NonNull TaskForm taskForm) {
        TodoTask task = new TodoTask();
        TodoStatus draft = getStatus(TodoStatus.Status.DRAFT);

        task.setStatus(draft);
        task.setCreationTime(LocalDateTime.now(ZoneId.systemDefault()));
        task.setCreatorId(creator.getId());
        task.setDeadline(taskForm.getDeadline());
        task.setHeader(taskForm.getHeader());
        task.setDescription(taskForm.getDescription());
        task.setManager(creator);
        task.setStack(taskForm.getStack());
        task.setUnitId(taskForm.getUnitId());

        return task;
    }

    private TodoStatus getStatus(TodoStatus.Status status) {
        if (status == null) {
            throw new RuntimeException("Статус не может быть null");
        }
        return todoStatusRepository.findById(status.getCode())
                .orElseThrow(() -> new RuntimeException("Нарушение данных: не найден статус " + status));
    }

    private void checkManagerRightsOnTask(Manager manager, TodoTask task) throws BadRequestException {
        Manager taskManager = task.getManager();
        if (taskManager == null || !manager.getId().equals(taskManager.getId())) {
            throw new BadRequestException("Невозможно опубликовать: за задачу отвечает другой менеджер");
        }
    }

    private void sendWorkerResponse(TodoTask task, Worker worker, String message) {
        WorkerResponse response = new WorkerResponse();
        response.setWorker(worker);
        response.setTask(task);
        response.setCreationTime(LocalDateTime.now(ZoneId.systemDefault()));
        response.setMessage(message);
        responseRepository.save(response);
    }

    private Long generateCompositeId(@NonNull Long taskId, @NonNull Integer workerId) {
        return (taskId * 10_000_000) + workerId;
    }
}

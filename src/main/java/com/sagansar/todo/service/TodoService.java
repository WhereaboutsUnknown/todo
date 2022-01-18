package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.WarningException;
import com.sagansar.todo.model.external.TaskForm;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.work.*;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.model.worker.WorkerResponse;
import com.sagansar.todo.repository.TodoStatusRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.repository.WorkerGroupTaskRepository;
import com.sagansar.todo.repository.WorkerResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TodoService {

    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);

    private final TodoTaskRepository todoTaskRepository;

    private final WorkerGroupTaskRepository workerGroupTaskRepository;

    private final TodoStatusRepository todoStatusRepository;

    private final DialogService dialogService;

    private final NotificationService notificationService;

    private final FileService fileService;

    private final WorkerResponseRepository responseRepository;

    public TodoService(TodoTaskRepository todoTaskRepository,
                       WorkerGroupTaskRepository workerGroupTaskRepository,
                       TodoStatusRepository todoStatusRepository,
                       NotificationService notificationService,
                       DialogService dialogService,
                       FileService fileService,
                       WorkerResponseRepository responseRepository) {
        this.workerGroupTaskRepository = workerGroupTaskRepository;
        this.todoStatusRepository = todoStatusRepository;
        this.todoTaskRepository = todoTaskRepository;
        this.notificationService = notificationService;
        this.dialogService = dialogService;
        this.fileService = fileService;
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
        TodoTask task = newTask(creator, taskForm);
        return todoTaskRepository.save(task);
    }

    /**
     * Create new task draft from archived task
     *
     * @param creator manager profile that is an applier of current form
     * @param template archived task
     * @param deadline deadline date and time
     * @return created task
     */
    public TodoTask createTask(@NonNull Manager creator, @NonNull ArchivedTask template, @NonNull LocalDateTime deadline) {
        TodoTask task = newTask(creator, template);
        task.setDeadline(deadline);
        return todoTaskRepository.save(task);
    }

    public TodoTask createTask(@NonNull Manager creator) {
        TodoTask task = newTask(creator);
        return todoTaskRepository.save(task);
    }

    /**
     * Publish created task
     *
     * @param manager Manager of current task
     * @param taskId task ID
     * @param visibleToAll false if only invited Workers see this task, true if everyone does
     * @return published task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask publishTask(@NonNull Manager manager, @NonNull Long taskId, boolean visibleToAll) throws BadRequestException {
        TodoTask task = getValidTask(taskId, Set.of(
                TodoStatus.Status.DRAFT
        ), "Невозможно опубликовать: задача не является черновиком!");
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
        TodoTask task = getValidTask(taskId, Set.of(
                TodoStatus.Status.TODO, TodoStatus.Status.DISCUSSION
        ),"Отклики на эту задачу уже закрыты!");
        if (!task.isVisibleToAll()) {
            throw new BadRequestException("Стать исполнителем для этой задачи можно только по приглашению!");
        }
        sendWorkerResponse(task, worker, message);
        dialogService.createDialog(task, worker.getUser(), message);

        if (!task.is(TodoStatus.Status.DISCUSSION)) {
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
     * @param invite accepted invite
     * @return success message
     */
    public String processAcceptedInvite(@NonNull Invite invite) {
        if (invite.isAccepted()) {
            addWorkerToTask(invite.getTask(), invite.getWorker());
        }
        return "задача успешно поручена Вам!";
    }

    /**
     * Set Worker as responsible for task
     *
     * @param manager Manager of current task
     * @param taskId task ID
     * @param worker Worker
     * @return saved task
     * @throws BadRequestException if worker is not associated with this task or in case of incorrect task ID
     */
    public TodoTask setWorkerResponsible(@NonNull Manager manager, @NonNull Long taskId, @NonNull Worker worker) throws BadRequestException {
        TodoTask task = getValidTask(taskId);
        checkManagerRightsOnTask(manager, task);
        WorkerGroupTask link = workerGroupTaskRepository.findById(generateCompositeId(task.getId(), worker.getId()))
                .orElseThrow(() -> new BadRequestException("Работник не является исполнителем данной задачи!"));
        link.setResponsible(true);
        task.setWorker(worker);
        workerGroupTaskRepository.save(link);
        return todoTaskRepository.save(task);
    }

    /**
     * Remove Worker from task
     *
     * @param manager Manager of current task
     * @param worker Worker
     * @param taskId task ID
     * @return changed task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask deleteWorkerFromTask(@NonNull Manager manager, @NonNull Worker worker, @NonNull Long taskId) throws BadRequestException {
        TodoTask task = getValidTask(taskId, Set.of(
                TodoStatus.Status.DRAFT, TodoStatus.Status.TODO, TodoStatus.Status.DISCUSSION, TodoStatus.Status.GO
        ), "Невозможно удалить сотрудника с выполненной задачи! Информация обо всех исполнителях этой задачи будет неизбежно помещена в архив");
        checkManagerRightsOnTask(manager, task);
        Optional<WorkerGroupTask> link = workerGroupTaskRepository.findById(generateCompositeId(task.getId(), worker.getId()));
        if (link.isEmpty()) {
            logger.warn("Невозможно удалить работника {} с задачи {}: он не является исполнителем данной задачи", worker.getName(), taskId);
            return task;
        }
        if (task.getWorker() != null && worker.getId().equals(task.getWorker().getId())) {
            task.setWorker(null);
        }
        workerGroupTaskRepository.delete(link.get());
        return task;
    }

    /**
     * Cancel task and remove all Workers from it
     *
     * @param manager Manager of current task
     * @param taskId task ID
     * @return cancelled task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask cancel(@NonNull Manager manager, @NonNull Long taskId) throws BadRequestException {
        TodoTask task = getValidTask(taskId, Set.of(
                TodoStatus.Status.TODO, TodoStatus.Status.DISCUSSION, TodoStatus.Status.GO, TodoStatus.Status.DONE, TodoStatus.Status.REVIEW
        ), "Эту задачу невозможно отменить! Попробуйте архивировать или удалить ее");
        checkManagerRightsOnTask(manager, task);
        workerGroupTaskRepository.deleteAll(
                workerGroupTaskRepository.findAllByTaskId(taskId).stream()
                    .peek(workerGroupTask -> notificationService.sendCancelledNotification(
                            workerGroupTask.getWorker().getUser(),
                            workerGroupTask.getTask().getHeader()))
                    .collect(Collectors.toList()));
        responseRepository.saveAll(task.getResponses().stream()
                        .peek(WorkerResponse::decline)
                        .peek(response -> notificationService.sendResponseDeclinedNotification(response.getWorker().getUser(), task.getHeader()))
                        .collect(Collectors.toList()));
        task.setWorker(null);

        TodoStatus status = getStatus(TodoStatus.Status.CANCELED);
        task.setStatus(status);

        return todoTaskRepository.save(task);
    }

    /**
     * Mark task as done
     *
     * @param worker Worker
     * @param taskId task ID
     * @return changed task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask done(@NonNull Worker worker, @NonNull Long taskId) throws BadRequestException {
        TodoTask task = getValidTask(taskId, Set.of(
                TodoStatus.Status.GO
        ), "Только задачи в работе могут быть помечены, как выполненные!");
        checkWorkerRightsOnTask(worker, task);
        Manager taskManager = task.getManager();
        if (taskManager == null || taskManager.getUser() == null) {
            throw new BadRequestException("За задачей не закреплен менеджер");
        }
        TodoStatus done = getStatus(TodoStatus.Status.DONE);
        task.setStatus(done);
        todoTaskRepository.save(task);
        notificationService.sendTaskDoneNotification(taskManager.getUser(), task.getHeader());
        return task;
    }

    /**
     * Make decision on work result for task
     *
     * @param manager Manager
     * @param taskId task ID
     * @param approved work result approved/task returned
     * @return changed task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask review(@NonNull Manager manager, @NonNull Long taskId, boolean approved) throws BadRequestException {
        TodoTask task = getValidTask(taskId, Set.of(
                TodoStatus.Status.DONE
        ), "Можно выносить решение только по готовым задачам!");
        checkManagerRightsOnTask(manager, task);
        TodoStatus decision = getStatus(approved ? TodoStatus.Status.APPROVED : TodoStatus.Status.GO);
        task.setStatus(decision);
        todoTaskRepository.save(task);
        workerGroupTaskRepository.findAllByTaskId(taskId).stream()
                .map(WorkerGroupTask::getWorker)
                .map(Worker::getUser)
                .forEach(user -> notificationService.sendWorkReviewNotification(user, task.getHeader(), approved));
        return task;
    }

    /**
     * Get completed task for archiving
     *
     * @param manager Manager
     * @param taskId task ID
     * @return valid task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask getTaskForArchiving(@NonNull Manager manager, @NonNull Long taskId) throws BadRequestException {
        TodoTask task = getValidTask(taskId, Set.of(
                TodoStatus.Status.APPROVED, TodoStatus.Status.GO, TodoStatus.Status.DONE
        ), "Можно архивировать только задачи, которые уже в работе или выполнены!");
        checkManagerRightsOnTask(manager, task);
        return task;
    }

    /**
     * Accept Worker response on task
     *
     * @param manager Manager
     * @param task task
     * @param worker Worker
     * @throws BadRequestException in case of denied access
     */
    public void acceptWorkerForTask(@NonNull Manager manager, @NonNull TodoTask task, @NonNull Worker worker) throws BadRequestException {
        checkManagerRightsOnTask(manager, task);
        addWorkerToTask(task, worker);
        notificationService.sendResponseAcceptedNotification(worker.getUser(), task.getHeader());
    }

    /**
     * Set task Manager
     *
     * @param manager Manager
     * @param taskId task ID
     * @return changed task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask setTaskManager(@NonNull Manager manager, @NonNull Long taskId) throws BadRequestException {
        TodoTask task = getValidTask(taskId);
        Manager previous = task.getManager();
        task.setManager(manager);
        if (previous != null) {
            notificationService.sendTaskManagerNotification(previous.getUser(), task.getHeader(), false);
        }
        notificationService.sendTaskManagerNotification(manager.getUser(), task.getHeader(), true);
        return todoTaskRepository.save(task);
    }

    /**
     * Remove Manager from task
     *
     * @param supervisor Unit supervisor
     * @param taskId task ID
     * @return changed task
     * @throws BadRequestException in case of invalid task ID
     */
    public TodoTask removeTaskManager(@NonNull Manager supervisor, @NonNull Long taskId) throws BadRequestException {
        TodoTask task = getValidTask(taskId);
        if (!supervisor.getUnit().getId().equals(task.getUnit().getId())) {
            throw new BadRequestException("Задача относится к другому отделу!");
        }
        Manager manager = task.getManager();
        if (supervisor.getId().equals(manager.getId())) {
            return task;
        }
        task.setManager(supervisor);
        notificationService.sendTaskManagerNotification(manager.getUser(), task.getHeader(), false);
        return todoTaskRepository.save(task);
    }

    public TodoTask editTask(@NonNull Manager manager, @NonNull Long taskId, @NonNull TaskForm taskForm) throws BadRequestException {
        TodoTask draft = getValidTask(taskId, Set.of(TodoStatus.Status.DRAFT), "Выбранная задача - не черновик!");
        checkManagerRightsOnTask(manager, draft);
        draft.setDeadline(taskForm.getDeadline());
        draft.setStack(taskForm.getStack());
        draft.setDescription(taskForm.getDescription());
        draft.setHeader(taskForm.getHeader());
        draft.setPlannedStart(taskForm.getPlannedStart());
        return todoTaskRepository.save(draft);
    }

    public void deleteDraft(@NonNull Manager manager, @NonNull Long taskId) throws BadRequestException {
        TodoTask draft = getValidTask(taskId, Set.of(TodoStatus.Status.DRAFT), "Выбранная задача - не черновик!");
        checkManagerRightsOnTask(manager, draft);
        fileService.deleteTaskFiles(draft);
        todoTaskRepository.delete(draft);
    }

    public TodoTask openAsDraft(@NonNull TodoTask task, @NonNull Manager manager) throws BadRequestException {
        if (!task.is(TodoStatus.Status.CANCELED)) {
            throw new BadRequestException("Задача должна быть отменена, чтобы открыть ее, как черновик!");
        }
        task.setStatus(getStatus(TodoStatus.Status.DRAFT));
        task.setManager(manager);
        return todoTaskRepository.save(task);
    }

    public void checkUserRightsOnTaskAsManager(@NonNull Integer userId, @NonNull Long taskId) throws BadRequestException {
        if (!todoTaskRepository.existsByIdAndManagerUserId(taskId, userId)) {
            throw new BadRequestException("Недостаточно прав для доступа к задаче!");
        }
    }

    public TodoTask getTaskForInvite(@NonNull Long taskId) throws BadRequestException {
        return getValidTask(taskId, Set.of(TodoStatus.Status.TODO, TodoStatus.Status.DISCUSSION), "Работников можно приглашать только в еще не начатые задачи!");
    }

    /**
     * Validate TodoTask for existence and having one of valid statuses and get valid one
     *
     * @param taskId task ID
     * @return existing TodoTask with status
     * @throws BadRequestException in case of no such task or task having no status
     */
    private TodoTask getValidTask(@NonNull Long taskId, Set<TodoStatus.Status> validStatuses, String ifInvalid) throws BadRequestException {
        TodoTask task = getValidTask(taskId);
        TodoStatus.Status status = task.getStatus().status();
        if (validStatuses.contains(status)) {
            return task;
        }
        throw new BadRequestException(ifInvalid);
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
                .orElseThrow(() -> new BadRequestException("Задача " + taskId + " не найдена", HttpStatus.NOT_FOUND));
        TodoStatus status = task.getStatus();
        if (status == null) {
            throw new BadRequestException("У задачи [" + taskId + "] отсутствует статус!");
        }
        return task;
    }

    /**
     * Add Worker to task group, set as responsible if no one responded yet
     *
     * @param task task
     * @param worker Worker
     */
    private void addWorkerToTask(@NonNull TodoTask task, @NonNull Worker worker) {
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
            if (!task.is(TodoStatus.Status.GO)) {
                TodoStatus go = todoStatusRepository.getById(TodoStatus.Status.GO.getCode());
                task.setStatus(go);
                todoTaskRepository.save(task);
            }
        }
    }

    /**
     * Create new task
     *
     * @param creator manager profile that is an applier of current form
     * @param template task data source
     * @return created task
     */
    private TodoTask newTask(@NonNull Manager creator, @NonNull TaskTemplate template) {
        TodoTask task = newTask(creator);

        task.setDeadline(template.getDeadline());
        task.setHeader(template.getHeader());
        task.setDescription(template.getDescription());
        task.setStack(template.getStack());

        return task;
    }

    private TodoTask newTask(@NonNull Manager creator) {
        TodoTask task = new TodoTask();
        TodoStatus draft = getStatus(TodoStatus.Status.DRAFT);

        task.setStatus(draft);
        task.setCreationTime(LocalDateTime.now(ZoneId.systemDefault()));
        task.setCreator(creator);
        task.setManager(creator);
        task.setUnit(creator.getUnit());

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
            throw new BadRequestException("За задачу отвечает другой менеджер");
        }
    }

    private void checkWorkerRightsOnTask(Worker worker, TodoTask task) throws BadRequestException {
        WorkerGroupTask workerGroupTask = workerGroupTaskRepository.findByTaskIdAndResponsibleTrue(task.getId())
                .orElseThrow(() -> new BadRequestException("Не найден ответственный исполнитель!"));
        if (!worker.getId().equals(workerGroupTask.getWorker().getId())) {
            throw new BadRequestException("За задачу отвечает другой исполнитель");
        }
    }

    private void sendWorkerResponse(TodoTask task, Worker worker, String message) {
        if (responseRepository.existsByWorkerIdAndTaskId(worker.getId(), task.getId())) {
            throw new WarningException("Отклик уже был отправлен!");
        }
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

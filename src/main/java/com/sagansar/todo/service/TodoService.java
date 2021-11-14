package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.TodoStatusRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class TodoService {

    private final TodoTaskRepository todoTaskRepository;

    private final TodoStatusRepository todoStatusRepository;

    private final DialogService dialogService;

    private final NotificationService notificationService;

    public TodoService(TodoTaskRepository todoTaskRepository,
                       TodoStatusRepository todoStatusRepository,
                       NotificationService notificationService,
                       DialogService dialogService) {
        this.todoStatusRepository = todoStatusRepository;
        this.todoTaskRepository = todoTaskRepository;
        this.notificationService = notificationService;
        this.dialogService = dialogService;
    }

    public void createTask(Manager creator) {

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
    public String claimTask(@NonNull Worker worker, @NonNull Long taskId, String message) throws BadRequestException {
        TodoTask task = getValidTaskForClaim(taskId);
        dialogService.createDialog(task, worker.getUser(), message);

        if (!TodoStatus.Status.DISCUSSION.equals(task.getStatus().status())) {
            TodoStatus discussion = todoStatusRepository.findById(TodoStatus.Status.DISCUSSION.getCode())
                    .orElseThrow(() -> new RuntimeException("Нарушение данных: не найден статус " + TodoStatus.Status.DISCUSSION));
            task.setStatus(discussion);
            todoTaskRepository.save(task);
        }
        notificationService.sendTaskClaimNotification(task.getManager().getUser(), task.getHeader(), worker.getName());

        return "Отклик успешно отправлен!";
    }

    /**
     * Validate TodoTask for claim step and get valid one
     *
     * @param taskId task ID
     * @return valid TodoTask
     * @throws BadRequestException in case of invalid task ID
     */
    private TodoTask getValidTaskForClaim(@NonNull Long taskId) throws BadRequestException {
        TodoTask task = todoTaskRepository.findById(taskId)
                .orElseThrow(() -> new BadRequestException("Задача " + taskId + " не найдена"));
        TodoStatus status = task.getStatus();
        if (status == null) {
            throw new BadRequestException("У задачи [" + taskId + "] отсутствует статус!");
        }
        TodoStatus.Status statusEnum = status.status();
        if (!TodoStatus.Status.DISCUSSION.equals(statusEnum) && !TodoStatus.Status.TODO.equals(statusEnum)) {
            throw new BadRequestException("Задача [" + taskId + "] имеет некорректный статус: " + statusEnum);
        }
        return task;
    }
}

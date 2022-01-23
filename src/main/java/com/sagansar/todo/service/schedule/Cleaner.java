package com.sagansar.todo.service.schedule;

import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.work.WorkerGroupTask;
import com.sagansar.todo.model.worker.WorkerResponse;
import com.sagansar.todo.repository.*;
import com.sagansar.todo.service.FileService;
import com.sagansar.todo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class Cleaner {

    private final NotificationRepository notificationRepository;

    private final InviteRepository inviteRepository;

    private final InviteKeyRepository inviteKeyRepository;

    private final WorkerResponseRepository workerResponseRepository;

    private final TodoTaskRepository todoTaskRepository;

    private final WorkerGroupTaskRepository workerGroupTaskRepository;

    private final NotificationService notificationService;

    private final FileService fileService;

    @Scheduled(cron = "0 0 1,13 * * *")
    public void cleanNotifications() {
        LocalDateTime twelveHoursAgo = LocalDateTime.now(ZoneId.systemDefault()).minusHours(12L);
        notificationRepository.deleteAllByReadTimeNotNullAndReadTimeBefore(twelveHoursAgo);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanInvites() {
        List<Invite> invites = inviteRepository.findAllByCheckedTrue();
        invites = invites.stream()
                .filter(invite -> !invite.getTask().is(TodoStatus.Status.TODO) && !invite.getTask().is(TodoStatus.Status.DISCUSSION) && !invite.getTask().is(TodoStatus.Status.GO))
                .collect(Collectors.toList());
        deleteInvites(invites);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanResponses() {
        List<WorkerResponse> responses = workerResponseRepository.findAllByResponseTimeBefore(LocalDateTime.now(ZoneId.systemDefault()).minusDays(1L));
        deleteResponses(responses);
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void cleanTasks() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        List<TodoTask> archivedTasks = todoTaskRepository.findAllByStatusCodeAndLastChangeDateBefore(TodoStatus.Status.ARCHIVE.getCode(), now.minusDays(1L));
        deleteTasks(archivedTasks);
        List<TodoTask> canceledTasks = todoTaskRepository.findAllByStatusCodeAndLastChangeDateBefore(TodoStatus.Status.CANCELED.getCode(), now.minusDays(7L));
        deleteTasks(canceledTasks);
        canceledTasks.stream()
                .map(TodoTask::getManager)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(manager -> notificationService.sendCanceledTasksRemoved(manager.getUser()));
    }

    private void deleteInvites(Collection<Invite> invites) {
        if (invites != null) {
            invites.forEach(invite -> inviteKeyRepository.deleteAllByInviteId(invite.getId()));
            inviteRepository.deleteAll(invites);
        }
    }

    private void deleteResponses(Collection<WorkerResponse> responses) {
        if (responses != null) {
            workerResponseRepository.deleteAll(responses);
        }
    }

    private void deleteGroups(Collection<WorkerGroupTask> taskWorkers) {
        if (taskWorkers != null) {
            workerGroupTaskRepository.deleteAll(taskWorkers);
        }
    }

    private void deleteTasks(List<TodoTask> tasks) {
        if (tasks != null) {
            for (TodoTask task : tasks) {
                deleteInvites(task.getInvites());
                deleteResponses(task.getResponses());
                deleteGroups(task.getGroup());
                fileService.deleteTaskFiles(task);
            }
            todoTaskRepository.deleteAll(tasks);
        }
    }
}

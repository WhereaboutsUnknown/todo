package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.WarningException;
import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.InviteRepository;
import com.sagansar.todo.repository.WorkerRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InviteService {

    private static final Logger logger = LoggerFactory.getLogger(InviteService.class);

    private final InviteRepository inviteRepository;

    private final WorkerRepository workerRepository;

    private final NotificationService notificationService;

    private final SocialMediaService socialMediaService;

    public void sendInvitesToAll(List<Integer> workers, @NonNull TodoTask task) {
        if (workers == null || workers.isEmpty()) {
            throw new WarningException("Приглашения не отправлены: не выбрано ни одного исполнителя!");
        }
        inviteRepository.saveAll(
            workers.stream()
                .map(workerRepository::findById)
                .flatMap(Optional::stream)
                .peek(worker -> notificationService.sendInviteNotification(worker.getUser(), task.getHeader()))
                .peek(worker -> socialMediaService.sendTelegramInvite(worker.getUser(), task.getHeader()))
                .map(worker -> createInvite(worker, task))
                .collect(Collectors.toList()));
    }

    private Invite createInvite(Worker worker, TodoTask task) {
        Invite invite = new Invite();
        invite.setTask(task);
        invite.setWorker(worker);
        invite.setCreationTime(LocalDateTime.now(ZoneId.systemDefault()));
        return invite;
    }
}

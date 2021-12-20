package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for creating and sending task invites to Workers
 */
@Service
@AllArgsConstructor
public class InviteService {

    private static final Logger logger = LoggerFactory.getLogger(InviteService.class);

    private final InviteRepository inviteRepository;

    private final WorkerRepository workerRepository;

    private final NotificationService notificationService;

    private final SocialMediaService socialMediaService;

    /**
     * Send task invites to all chosen Workers except for profiles without User and non-active profiles
     *
     * @param workers Worker ID list
     * @param task task
     */
    public List<Integer> sendInvitesToAll(List<Integer> workers, @NonNull TodoTask task) {
        if (workers == null || workers.isEmpty()) {
            throw new WarningException("Приглашения не отправлены: не выбрано ни одного исполнителя!");
        }
        List<Integer> invited = new ArrayList<>();
        workers.stream()
                .map(workerRepository::findById)
                .flatMap(Optional::stream)
                .filter(worker -> worker.getUser() != null)
                .filter(Worker::isActive)
                .peek(worker -> notificationService.sendInviteNotification(worker.getUser(), task.getHeader()))
                .map(worker -> createInvite(worker, task))
                .map(inviteRepository::save)
                .forEach(invite -> {
                    if (socialMediaService.sendTelegramInvite(invite)) {
                        invited.add(invite.getWorker().getId());
                    }
                });
        return invited;
    }

    public Invite processInviteAnswer(Long inviteId, boolean accept) throws BadRequestException {
        if (inviteId == null) {
            logger.error("Отсутствует ID приглашения");
            throw new BadRequestException("При обработке ответа возникла ошибка!");
        }
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new BadRequestException("Приглашение не найдено!"));
        invite.setAccepted(accept);
        invite.setChecked(true);
        return inviteRepository.save(invite);
        //TODO: уведомления о полученном ответе на приглашение
    }

    private Invite createInvite(Worker worker, TodoTask task) {
        Invite invite = new Invite();
        invite.setTask(task);
        invite.setWorker(worker);
        invite.setCreationTime(LocalDateTime.now(ZoneId.systemDefault()));
        return invite;
    }
}

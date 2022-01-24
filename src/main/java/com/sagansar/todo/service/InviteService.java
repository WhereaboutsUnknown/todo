package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.WarningException;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.InviteRepository;
import com.sagansar.todo.repository.WorkerRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

    private final SecurityService securityService;

    /**
     * Send task invites to all chosen Workers except for profiles without User and non-active profiles
     *
     * @param workers Worker ID list
     * @param task task
     */
    public List<Worker> sendInvitesToAll(List<Integer> workers, @NonNull TodoTask task) {
        if (workers == null) {
            return Collections.emptyList();
        }
        List<Worker> invited = new ArrayList<>();
        workers.stream()
                .map(workerRepository::findById)
                .flatMap(Optional::stream)
                .filter(worker -> worker.getUser() != null)
                .filter(Worker::isActive)
                .map(worker -> createInvite(worker, task))
                .filter(Objects::nonNull)
                .map(inviteRepository::save)
                .forEach(invite -> {
                    socialMediaService.sendTelegramInvite(invite);
                    invited.add(invite.getWorker());
                    notificationService.sendInviteNotification(invite.getWorker().getUser(), task.getHeader());
                });
        return invited;
    }

    /**
     * Process Worker answer on task invite
     *
     * @param inviteId invite ID
     * @param accept task accepted/declined
     * @return invite
     * @throws BadRequestException in case of invalid invite ID
     */
    public Invite processInviteAnswer(Long inviteId, boolean accept) throws BadRequestException {
        if (inviteId == null) {
            logger.error("Отсутствует ID приглашения");
            throw new BadRequestException("При обработке ответа возникла ошибка!");
        }
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new BadRequestException("Приглашение не найдено!"));
        TodoTask task = invite.getTask();
        if (task == null || statusInvalid(task)) {
            invite.setAccepted(false);
            invite.setChecked(true);
            inviteRepository.save(invite);
            throw new BadRequestException("Эта задача больше не доступна!");
        }
        invite.setAccepted(accept);
        invite.setChecked(true);
        Manager manager = invite.getTask().getManager();
        if (manager != null && manager.getUser() != null && manager.isActive()) {
            notificationService.sendInviteResponseNotification(
                    manager.getUser(),
                    invite.getTask().getHeader(),
                    invite.getWorker().getName(),
                    accept
            );
        } else {
            logger.error("У задачи {} отсутствует менеджер", invite.getTask().getId());
        }
        return inviteRepository.save(invite);
    }

    public List<Invite> findInvitesOnTask(@NonNull Long taskId) {
        return inviteRepository.findAllByTaskId(taskId);
    }

    public List<Invite> findWorkerInvites(@NonNull Integer workerId) {
        return inviteRepository.findAllByWorkerIdAndCheckedFalse(workerId);
    }

    public TodoTask cancelInvite(@NonNull Long taskId, @NonNull Integer workerId, @NonNull Manager manager) throws BadRequestException {
        Invite invite = inviteRepository.findByWorkerIdAndTaskId(workerId, taskId)
                .orElseThrow(() -> new BadRequestException("Приглашение не найдено!"));
        if (!manager.getId().equals(invite.getTask().getManager().getId()) && !securityService.checkUserRights(RoleEnum.SUPERVISOR)) {
            throw new BadRequestException("Невозможно отменить приглашение: это задача другого куратора!", HttpStatus.FORBIDDEN);
        }
        try {
            securityService.destroyInviteTokens(invite.getId());
            inviteRepository.delete(invite);
            notificationService.sendInviteCancelledNotification(invite.getWorker().getUser(), invite.getTask().getHeader());
            TodoTask task = invite.getTask();
            task.getInvites().remove(invite);
            return task;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new BadRequestException("Не удалось отменить приглашение!");
        }
    }

    public void cancelAllInvites(@NonNull Long taskId, @NonNull Manager manager) throws BadRequestException {
        List<Invite> invites = findInvitesOnTask(taskId);
        if (!invites.isEmpty()) {
            if (!manager.getId().equals(invites.get(0).getTask().getManager().getId())) {
                throw new BadRequestException("Невозможно отменить приглашение: это задача другого куратора!", HttpStatus.FORBIDDEN);
            }
            for (Invite invite : invites) {
                securityService.destroyInviteTokens(invite.getId());
            }
            inviteRepository.deleteAll(invites);
            for (Invite invite : invites) {
                notificationService.sendInviteCancelledNotification(invite.getWorker().getUser(), invite.getTask().getHeader());
            }
        }
    }

    public void deleteExpiredInvite(@NonNull Long taskId, @NonNull Integer workerId) {
        Invite invite = inviteRepository.findByWorkerIdAndTaskId(workerId, taskId).orElse(null);
        if (invite != null && invite.isChecked()) {
            securityService.destroyInviteTokens(invite.getId());
            inviteRepository.delete(invite);
        }
    }

    private Invite createInvite(Worker worker, TodoTask task) {
        if (inviteRepository.existsByWorkerIdAndTaskId(worker.getId(), task.getId())) {
            logger.error("Сотрудник {} уже приглашен на задачу {}", worker.getId(), task.getId());
            return null;
        }
        Invite invite = new Invite();
        invite.setTask(task);
        invite.setWorker(worker);
        invite.setCreationTime(LocalDateTime.now(ZoneId.systemDefault()));
        return invite;
    }

    private boolean statusInvalid(TodoTask task) {
        return !task.is(TodoStatus.Status.TODO) && !task.is(TodoStatus.Status.DISCUSSION) && !task.is(TodoStatus.Status.GO);
    }
}

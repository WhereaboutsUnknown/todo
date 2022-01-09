package com.sagansar.todo.service;

import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.Notification;
import com.sagansar.todo.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendTaskClaimNotification(@NonNull User user, String taskHeader, String worker) {
        String message = "По задаче " + taskHeader + " поступил новый отклик от исполнителя " + worker + "!";
        sendNotification(user, message);
    }

    public void sendInviteNotification(@NonNull User user, String taskHeader) {
        String message = "Для вас есть новая задача! \"" + taskHeader + "\"";
        sendNotification(user, message);
    }

    public void sendTaskDoneNotification(@NonNull User user, String taskHeader) {
        String message = "Ваша задача \"" + taskHeader + "\" была выполнена!";
        sendNotification(user, message);
    }

    public void sendResponseAcceptedNotification(@NonNull User user, String taskHeader) {
        String message = "Вы назначены исполнителем задачи \"" + taskHeader + "\"!";
        sendNotification(user, message);
    }

    public void sendResponseDeclinedNotification(@NonNull User user, String taskHeader) {
        String message = "Ваш отклик на задачу \"" + taskHeader + "\" был отклонен";
        sendNotification(user, message);
    }

    public void sendCancelledNotification(@NonNull User user, String taskHeader) {
        String message = "Задача \"" + taskHeader + "\" была снята заказчиком, её выполнение больше не требуется";
        sendNotification(user, message);
    }

    public void sendInviteResponseNotification(@NonNull User user, String taskHeader, String worker, boolean accepted) {
        String message = "Исполнитель " + worker + " "
                + (accepted ? "принял приглашение на задачу" : "отказался от задачи")
                + " \"" + taskHeader + "\"!";
        sendNotification(user, message);
    }

    public void sendTaskManagerNotification(@NonNull User user, String taskHeader, boolean isTaskManager) {
        String message = "Вы " + (isTaskManager ? "назначены" : "больше не являетесь")
                + " ответственным за задачу \"" + taskHeader + "\"!";
        sendNotification(user, message);
    }

    public void sendWorkReviewNotification(@NonNull User user, String taskHeader, boolean approved) {
        String message = "Задача \"" + taskHeader + "\" " + (approved ? "принята заказчиком!" : "возвращена на доработку!");
        sendNotification(user, message);
    }

    public void sendNotification(@NonNull User user, String message) {
        Notification notification = createNotification(user, message);
        notificationRepository.save(notification);
    }

    public void deleteNotifications(@NonNull List<Long> ids) {
        notificationRepository.deleteAllById(ids);
    }

    private Notification createNotification(@NonNull User user, String message) {
        Notification notification = new Notification();
        notification.setNote(message);
        notification.setUser(user);
        notification.setFireTime(LocalDateTime.now(ZoneId.systemDefault()));
        return notification;
    }
}

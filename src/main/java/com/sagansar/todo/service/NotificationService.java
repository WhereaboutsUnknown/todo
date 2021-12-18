package com.sagansar.todo.service;

import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.Notification;
import com.sagansar.todo.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    private final RestOperations restOperations;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        restOperations = new RestTemplate();
    }

    public void sendTaskClaimNotification(@NonNull User user, String taskHeader, String worker) {
        String message = "По задаче " + taskHeader + " поступил новый отклик от исполнителя " + worker + "!";
        sendNotification(user, message);
    }

    public void sendInviteNotification(@NonNull User user, String taskHeader) {
        String message = "Для вас есть новая задача! \"" + taskHeader + "\"";
        sendNotification(user, message);
        sendExternalInviteNotifications(user, taskHeader);
    }

    public void sendNotification(@NonNull User user, String message) {
        Notification notification = createNotification(user, message);
        notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(@NonNull User user) {
        return notificationRepository.findAllByUserId(user.getId());
    }

    public void deleteNotifications(@NonNull List<Long> ids) {
        notificationRepository.deleteAllById(ids);
    }

    private Notification createNotification(@NonNull User user, String message) {
        Notification notification = new Notification();
        notification.setNote(message);
        notification.setUser(user);
        return notification;
    }

    private void sendExternalInviteNotifications(User user, String task) {
        //TODO: use pengrad Telegram Bot API for sending notifications with URLs for accepting invite

        //TODO: должна приходить ссылка (в тг?), по которой можно принять инвайт (секьюрити должна пропустить), InviteController
        logger.warn("Telegram notifications are still in progress!");

        ////////////////////////////////////
    }
}

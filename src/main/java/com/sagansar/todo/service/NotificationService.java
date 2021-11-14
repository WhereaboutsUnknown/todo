package com.sagansar.todo.service;

import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.Notification;
import com.sagansar.todo.repository.NotificationRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendTaskClaimNotification(@NonNull User user, String taskHeader, String worker) {
        String message = "По задаче " + taskHeader + " поступил новый отклик от исполнителя " + worker + "!";
        sendNotification(user, message);
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
}

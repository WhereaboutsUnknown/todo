package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.NotificationMessage;
import com.sagansar.todo.model.work.Notification;

import java.time.format.DateTimeFormatter;

public class NotificationMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, hh : mm");

    public static NotificationMessage notificationToMessage(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setNote(notification.getNote());
        notificationMessage.setId(notification.getId());
        notificationMessage.setFireTime(notification.getFireTime().format(formatter));
        notificationMessage.setRead(notification.getReadTime() != null);
        return notificationMessage;
    }
}

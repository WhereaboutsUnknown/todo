package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.NotificationMessage;
import com.sagansar.todo.model.work.Notification;

public class NotificationMapper {

    public static NotificationMessage notificationToMessage(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setNote(notification.getNote());
        notificationMessage.setId(notification.getId());
        return notificationMessage;
    }
}

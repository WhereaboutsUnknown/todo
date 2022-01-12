package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskWarning implements TaskAlert {

    private String warningAlert;

    public TaskWarning(String message) {
        this.warningAlert = message;
    }
}

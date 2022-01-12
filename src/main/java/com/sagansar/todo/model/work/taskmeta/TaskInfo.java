package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskInfo implements TaskAlert {

    private String infoAlert;

    public TaskInfo(String message) {
        this.infoAlert = message;
    }
}

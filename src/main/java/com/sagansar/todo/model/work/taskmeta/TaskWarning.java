package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskWarning implements TaskAlert {

    private String warning;

    public TaskWarning(String message) {
        this.warning = message;
    }
}

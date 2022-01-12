package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskError implements TaskAlert {

    private String errorAlert;

    public TaskError(String message) {
        this.errorAlert = message;
    }
}

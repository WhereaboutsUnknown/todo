package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskError implements TaskAlert {

    private String error;

    public TaskError(String message) {
        this.error = message;
    }
}

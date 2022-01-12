package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskInfo implements TaskAlert {

    private String info;

    public TaskInfo(String message) {
        this.info = message;
    }
}

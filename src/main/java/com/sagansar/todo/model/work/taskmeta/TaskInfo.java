package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskInfo implements TaskAlert {

    private String infoAlert;

    public TaskInfo(String message) {
        this.infoAlert = message;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isWarning() {
        return false;
    }

    @Override
    public boolean isInfo() {
        return true;
    }

    @Override
    public String alert() {
        return this.infoAlert;
    }
}

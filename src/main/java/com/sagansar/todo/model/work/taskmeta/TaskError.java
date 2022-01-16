package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskError implements TaskAlert {

    private String errorAlert;

    public TaskError(String message) {
        this.errorAlert = message;
    }

    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public boolean isWarning() {
        return false;
    }

    @Override
    public boolean isInfo() {
        return false;
    }

    @Override
    public String alert() {
        return this.errorAlert;
    }
}

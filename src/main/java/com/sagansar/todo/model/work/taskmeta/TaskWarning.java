package com.sagansar.todo.model.work.taskmeta;

import lombok.Data;

@Data
public class TaskWarning implements TaskAlert {

    private String warningAlert;

    public TaskWarning(String message) {
        this.warningAlert = message;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isWarning() {
        return true;
    }

    @Override
    public boolean isInfo() {
        return false;
    }

    @Override
    public String alert() {
        return this.warningAlert;
    }
}

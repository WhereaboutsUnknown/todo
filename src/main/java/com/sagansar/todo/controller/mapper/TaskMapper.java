package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;

public class TaskMapper {

    public static TaskShortDto taskToShort(TodoTask task) {
        TaskShortDto dto = new TaskShortDto();
        dto.setId(task.getId());
        dto.setHeader(task.getHeader());
        dto.setStack(task.getStack());
        dto.setDeadline(task.getDeadline());
        TodoStatus status = task.getStatus();
        if (status != null) {
            dto.setStatus(status.getStatusName());
        }
        return dto;
    }
}

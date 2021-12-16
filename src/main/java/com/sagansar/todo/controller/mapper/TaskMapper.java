package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.TaskFullDto;
import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;

public class TaskMapper {

    public static TaskShortDto taskToShort(TodoTask task) {
        if (task == null) {
            return null;
        }
        TaskShortDto dto = new TaskShortDto();
        dto.setId(task.getId());
        dto.setHeader(task.getHeader());
        dto.setStack(task.getStack());
        dto.setDeadline(task.getDeadline());
        TodoStatus status = task.getStatus();
        if (status != null) {
            dto.setStatus(status.getDescription());
        }
        return dto;
    }

    public static TaskFullDto taskToFull(TodoTask task) {
        if (task == null) {
            return null;
        }
        TaskFullDto dto = new TaskFullDto();
        dto.setId(task.getId());
        dto.setHeader(task.getHeader());
        dto.setDescription(task.getDescription());
        dto.setStack(task.getStack());
        dto.setDeadline(task.getDeadline());
        TodoStatus status = task.getStatus();
        if (status != null) {
            dto.setStatus(status.getDescription());
        }

        return dto;
    }
}

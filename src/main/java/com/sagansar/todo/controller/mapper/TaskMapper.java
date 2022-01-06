package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.TaskFullDto;
import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.work.WorkerGroupTask;

import java.util.stream.Collectors;

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
        dto.setCreator(PersonMapper.managerToName(task.getCreator()));
        dto.setManager(PersonMapper.managerToName(task.getManager()));
        dto.setUnit(UnitMapper.unitToBasic(task.getUnit()));
        dto.setWorker(PersonMapper.workerToName(task.getWorker()));
        dto.setGroup(task.getGroup().stream()
                .map(WorkerGroupTask::getWorker)
                .map(PersonMapper::workerToName)
                .collect(Collectors.toList()));
        dto.setFiles(task.getFiles().stream()
                .map(FileMapper::fileToBasic)
                .collect(Collectors.toList()));
        TodoStatus status = task.getStatus();
        if (status != null) {
            dto.setStatus(status.getDescription());
        }

        return dto;
    }
}

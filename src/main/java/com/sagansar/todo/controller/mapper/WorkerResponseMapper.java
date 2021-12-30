package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.controller.dto.WorkerFullDto;
import com.sagansar.todo.controller.dto.WorkerResponseDto;
import com.sagansar.todo.model.worker.WorkerResponse;

public class WorkerResponseMapper {

    public static WorkerResponseDto responseToDto(WorkerResponse response) {
        if (response == null) {
            return null;
        }
        WorkerResponseDto dto = new WorkerResponseDto();
        dto.setId(response.getId());
        dto.setMessage(response.getMessage());
        dto.setCreationTime(response.getCreationTime());
        WorkerFullDto worker = WorkerMapper.workerToFullDto(response.getWorker());
        TaskShortDto task = TaskMapper.taskToShort(response.getTask());
        dto.setWorker(worker);
        dto.setTask(task);
        return dto;
    }
}

package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.PersonNameDto;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.worker.Worker;

public class PersonMapper {

    public static PersonNameDto managerToName(Manager manager) {
        PersonNameDto dto = new PersonNameDto();
        dto.setName(manager.getFullName());
        dto.setId(manager.getId());
        return dto;
    }

    public static PersonNameDto workerToName(Worker worker) {
        PersonNameDto dto = new PersonNameDto();
        dto.setName(worker.getName());
        dto.setId(worker.getId());
        return dto;
    }
}

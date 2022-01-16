package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.PersonNameDto;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.worker.Worker;

public class PersonMapper {

    public static PersonNameDto managerToName(Manager manager) {
        if (manager == null) {
            return null;
        }
        PersonNameDto dto = new PersonNameDto();
        dto.setName(manager.getFullName());
        dto.setId(manager.getId());
        dto.setAvatar(getAvatar(manager.getUser()));
        return dto;
    }

    public static PersonNameDto workerToName(Worker worker) {
        if (worker == null) {
            return null;
        }
        PersonNameDto dto = new PersonNameDto();
        dto.setName(worker.getName());
        dto.setId(worker.getId());
        dto.setAvatar(getAvatar(worker.getUser()));
        return dto;
    }

    private static Long getAvatar(User user) {
        if (user == null) {
            return 0L;
        }
        return user.getAvatar() == null ? 0L : user.getAvatar();
    }
}

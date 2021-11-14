package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.WorkerDto;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.worker.Worker;

public class WorkerMapper {

    public static WorkerDto workerToDto(Worker worker) {
        if (worker == null) {
            return null;
        }
        WorkerDto dto = new WorkerDto();
        dto.setId(worker.getId());
        dto.setName(worker.getName());
        dto.setAge(worker.getAge());
        User user = worker.getUser();
        if (user != null) {
            dto.setContacts(ContactsMapper.contactsToDto(user.getContacts()));
        }
        return dto;
    }
}

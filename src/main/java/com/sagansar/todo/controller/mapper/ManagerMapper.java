package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.ManagerDto;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;

public class ManagerMapper {

    public static ManagerDto managerToDto(Manager manager) {
        if (manager == null) {
            return null;
        }
        ManagerDto dto = new ManagerDto();
        dto.setId(manager.getId());
        dto.setName(manager.getFullName());
        dto.setAge(manager.getAge());
        User user = manager.getUser();
        if (user != null) {
            dto.setContacts(ContactsMapper.contactsToDto(user.getContacts()));
        }
        return dto;
    }
}

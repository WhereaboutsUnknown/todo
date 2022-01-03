package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.UserDto;
import com.sagansar.todo.model.general.Role;
import com.sagansar.todo.model.general.User;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static User dtoToUser(UserDto userDto, String encodedPassword) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(encodedPassword);
        user.setFirstName(userDto.getFirstName());
        user.setPatronym(userDto.getPatronym());
        user.setSurname(userDto.getSurname());
        return user;
    }

    public static UserDto userToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setPatronym(user.getPatronym());
        dto.setSurname(user.getSurname());
        dto.setBirthDate(user.getBirthDate());
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        if (roles.contains("MANAGER")) {
            dto.setRole("Менеджер");
        } else if (roles.contains("FREELANCER")) {
            dto.setRole("Сотрудник");
        }
        dto.setActive(user.isActive());
        return dto;
    }
}

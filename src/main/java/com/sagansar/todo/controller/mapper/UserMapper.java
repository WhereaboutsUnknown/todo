package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.UserDto;
import com.sagansar.todo.model.general.User;

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
}

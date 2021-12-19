package com.sagansar.todo.telegram.repository;

import com.sagansar.todo.telegram.model.CustomBotCommand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomBotCommandRepository extends JpaRepository<CustomBotCommand, String> {
}

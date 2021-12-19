package com.sagansar.todo.telegram.repository;

import com.sagansar.todo.telegram.model.TelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramChatRepository extends JpaRepository<TelegramChat, Long> {
    TelegramChat findDistinctByUsername(String username);
}

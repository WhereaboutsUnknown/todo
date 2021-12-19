package com.sagansar.todo.telegram.repository;

import com.sagansar.todo.telegram.model.BotResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotResponseRepository extends JpaRepository<BotResponse, Long> {
}

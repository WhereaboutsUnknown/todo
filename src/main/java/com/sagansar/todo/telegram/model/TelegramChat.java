package com.sagansar.todo.telegram.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "telegram_chat")
public class TelegramChat {

    @Id
    @Column(name = "chat_id")
    private Long id;

    @Column(name = "last_update")
    private LocalDateTime lastUpdateTime;

    @Column(name = "tg_user")
    private String username;
}

package com.sagansar.todo.telegram.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity(name = "telegram_bot_msg")
public class BotResponse {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "descr")
    private String description;
}

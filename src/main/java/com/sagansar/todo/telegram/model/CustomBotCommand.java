package com.sagansar.todo.telegram.model;

import com.sagansar.todo.model.general.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "telegram_custom")
public class CustomBotCommand {

    @Id
    @Column(name = "custom_cmd")
    private String command;

    @Column(name = "cmd_out")
    private String output;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "active")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "user_creator")
    private User creator;
}

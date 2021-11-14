package com.sagansar.todo.model.work;

import com.sagansar.todo.model.general.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "msg_text")
    private String text;

    @Column(name = "msg_time")
    private LocalDateTime time;

    @ManyToOne
    @JoinColumn(name = "dialog")
    private Dialog dialog;

    @ManyToOne
    @JoinColumn(name = "owner_user")
    private User user;
}

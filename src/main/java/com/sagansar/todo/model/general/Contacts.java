package com.sagansar.todo.model.general;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "contacts")
public class Contacts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "phone_num", length = 100)
    String phoneNumber;

    @Column(name = "email")
    String email;

    @Column(name = "vk_com")
    String vk;

    @Column(name = "telegram")
    String telegram;

    @Column(name = "facebook")
    String facebook;

    @Column(name = "other", length = 515)
    String other;

    @OneToOne
    @JoinColumn(name = "user_id")
    User user;
}

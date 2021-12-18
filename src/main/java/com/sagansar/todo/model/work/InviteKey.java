package com.sagansar.todo.model.work;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "invite_key")
public class InviteKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "crypto_key")
    private String key;

    @OneToOne
    @JoinColumn(name = "invite")
    private Invite invite;
}

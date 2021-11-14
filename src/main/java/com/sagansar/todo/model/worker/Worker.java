package com.sagansar.todo.model.worker;

import com.sagansar.todo.model.general.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "worker_profiles")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name")
    private String name;

    @Column(name = "short_descr")
    private String info;

    @Column(name = "active")
    private boolean active;

    public void block() {
        active = false;
    }

    public void unblock() {
        active = true;
    }

    public String getAge() {
        if (user != null) {
            return user.getAge();
        }
        return null;
    }
}

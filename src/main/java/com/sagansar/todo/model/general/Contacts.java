package com.sagansar.todo.model.general;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity(name = "contacts")
public class Contacts implements Serializable {

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

    public void copy(Contacts other) {
        if (other != null) {
            if (StringUtils.hasText(other.email)) {
                email = other.email;
            }
            if (StringUtils.hasText(other.vk)) {
                vk = other.vk;
            }
            if (StringUtils.hasText(other.phoneNumber)) {
                phoneNumber = other.phoneNumber;
            }
            if (StringUtils.hasText(other.email)) {
                telegram = other.telegram;
            }
            if (StringUtils.hasText(other.email)) {
                facebook = other.facebook;
            }
            if (StringUtils.hasText(other.email)) {
                this.other = other.other;
            }
        }
    }
}

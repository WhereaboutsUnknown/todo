package com.sagansar.todo.model.general;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Set;

@Getter
@Setter
@Entity(name = "user_accs")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Integer id;

    @Column(name = "username", length = 100, nullable = false, unique = true)
    protected String username;

    @Column(name = "user_secret", length = 515)
    protected String password;

    @Column(name = "first_name", length = 100)
    protected String firstName;

    @Column(name = "patronym", length = 100)
    protected String patronym;

    @Column(name = "surname", length = 100)
    protected String surname;

    @Column(name = "birth_date")
    protected LocalDate birthDate;

    @ManyToMany(fetch = FetchType.EAGER)
    protected Set<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    protected Contacts contacts;

    @Column(name = "active")
    protected boolean active;

    @Column(name = "avatar_id")
    protected Long avatar;

    public void block() {
        active = false;
    }

    public void unblock() {
        active = true;
    }

    public Integer getAge() {
        if (birthDate != null) {
            return Period.between(birthDate, LocalDate.now(ZoneId.systemDefault())).getYears();
        }
        return null;
    }

    public void copy(User other) {
        if (other != null) {
            if (StringUtils.hasText(other.firstName)) {
                firstName = other.firstName;
            }
            if (StringUtils.hasText(other.patronym)) {
                patronym = other.patronym;
            }
            if (StringUtils.hasText(other.surname)) {
                surname = other.surname;
            }
            if (other.birthDate != null) {
                birthDate = other.birthDate;
            }
            if (other.contacts != null) {
                if (contacts != null) {
                    contacts.copy(other.contacts);
                } else {
                    contacts = other.contacts;
                }
            }
        }
    }
}

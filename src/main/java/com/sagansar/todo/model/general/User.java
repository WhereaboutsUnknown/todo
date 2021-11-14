package com.sagansar.todo.model.general;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
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

    @OneToOne(mappedBy = "user")
    protected Contacts contacts;

    public String getAge() {
        if (birthDate != null) {
            int userAge = LocalDate.now(ZoneId.systemDefault()).getYear() - birthDate.getYear();
            return String.valueOf(userAge);
        }
        return null;
    }
}

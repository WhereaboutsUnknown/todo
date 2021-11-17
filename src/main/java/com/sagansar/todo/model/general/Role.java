package com.sagansar.todo.model.general;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity(name = "roles")
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "role_name", length = 100)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_accs_roles",
            joinColumns = { @JoinColumn(name = "roles_id") },
            inverseJoinColumns = { @JoinColumn(name = "user_accs_id") })
    private Set<User> users;

    @Override
    public String getAuthority() {
        return name;
    }
}

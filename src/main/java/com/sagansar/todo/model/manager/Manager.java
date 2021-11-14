package com.sagansar.todo.model.manager;

import com.sagansar.todo.model.general.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "task_managers")
public class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "active")
    private boolean active;

    public void block() {
        active = false;
    }

    public void unblock() {
        active = true;
    }

    public String getFullName() {
        if (user != null) {
            String surname = user.getSurname();
            String firstname = user.getFirstName();
            String patronym = user.getPatronym();
            List<String> nameComponents = new ArrayList<>();
            if (StringUtils.hasText(surname)) {
                nameComponents.add(surname);
            }
            if (StringUtils.hasText(firstname)) {
                nameComponents.add(firstname);
            }
            if (StringUtils.hasText(patronym)) {
                nameComponents.add(patronym);
            }
            return String.join(" ", nameComponents.toArray(new String[0]));
        }
        return null;
    }

    public String getAge() {
        if (user != null) {
            return user.getAge();
        }
        return null;
    }
}

package com.sagansar.todo.model.manager;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * Organization department or separate organization (has Managers)
 */
@Getter
@Setter
@Entity(name = "org_unit")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "unit_name")
    private String name;

    @Column(name = "descr")
    private String description;

    @OneToMany(mappedBy = "unit")
    private List<Manager> managers;
}

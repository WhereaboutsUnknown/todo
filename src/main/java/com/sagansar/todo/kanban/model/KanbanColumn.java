package com.sagansar.todo.kanban.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "kanban_column")
public class KanbanColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "col_name")
    private String name;

    @Column(name = "order")
    private Integer order;

    @Column(name = "is_finished")
    private Boolean finishing;

    @Column(name = "is_deletable")
    private Boolean deletable;

    @ManyToOne
    @JoinColumn(name = "board")
    private Kanban board;

    @OneToMany(mappedBy = "column")
    private List<KanbanTicket> tickets = new ArrayList<>();
}

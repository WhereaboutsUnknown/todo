package com.sagansar.todo.kanban.model;

import com.sagansar.todo.model.work.TodoTask;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "kanban_board")
public class Kanban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "task")
    private TodoTask task;

    @OneToMany(mappedBy = "board")
    private List<KanbanColumn> columns = new ArrayList<>();
}

package com.sagansar.todo.model.work;

import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.model.worker.Worker;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity(name = "todo_task")
public class TodoTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "header")
    private String header;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private Manager creator;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private TodoStatus status;

    @OneToMany(mappedBy = "task")
    private Set<WorkerGroupTask> group;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<TaskFile> files;

    @Column(name = "main_stack")
    private String stack;

    @Column(name = "created")
    private LocalDateTime creationTime;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "visible_all")
    private boolean visibleToAll;
}

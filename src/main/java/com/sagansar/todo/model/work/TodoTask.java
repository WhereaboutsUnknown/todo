package com.sagansar.todo.model.work;

import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.worker.Worker;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "creator_id")
    private Integer creatorId;

    @OneToOne
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @OneToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @Column(name = "unit_id")
    private Integer unitId;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private TodoStatus status;

    @Column(name = "main_stack")
    private String stack;

    @Column(name = "created")
    private LocalDateTime creationTime;

    @Column(name = "deadline")
    private LocalDateTime deadline;
}

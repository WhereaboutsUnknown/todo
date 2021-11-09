package com.sagansar.todo.model.work;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
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

    @Column(name = "worker_id")
    private Integer workerId;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "manager_id")
    private Integer managerId;

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

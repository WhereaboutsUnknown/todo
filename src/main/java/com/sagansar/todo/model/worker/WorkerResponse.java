package com.sagansar.todo.model.worker;

import com.sagansar.todo.model.work.TodoTask;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Отклик работника на открытую задачу
 */
@Getter
@Setter
@Entity(name = "worker_response")
public class WorkerResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "creation")
    private LocalDateTime creationTime;

    @ManyToOne
    @JoinColumn(name = "worker")
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "task")
    private TodoTask task;

    @Column(name = "checked")
    private boolean checked;
}

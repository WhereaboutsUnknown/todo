package com.sagansar.todo.model.worker;

import com.sagansar.todo.model.work.TodoTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    @Setter(AccessLevel.NONE)
    @Column(name = "responded")
    private boolean responded;

    @Setter(AccessLevel.NONE)
    @Column(name = "declined")
    private boolean declined;

    @Column(name = "response_time")
    private LocalDateTime responseTime;

    @Column(name = "decline_cause")
    private String declineCause;

    public void accept() {
        checked = true;
        responded = true;
        declined = false;
        setResponseTime();
    }

    public void decline(String message) {
        declineCause = message;
        decline();
    }

    public void decline() {
        checked = true;
        responded = true;
        declined = true;
        setResponseTime();
    }

    private void setResponseTime() {
        responseTime = LocalDateTime.now(ZoneId.systemDefault());
    }
}

package com.sagansar.todo.model.work;

import com.sagansar.todo.model.worker.Worker;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "worker_group_task")
public class WorkerGroupTask {

    @Id
    @Column(name = "id")
    private Long compositeId;

    @Column(name = "in_charge")
    private boolean responsible;

    @OneToOne
    @NonNull
    @JoinColumn(name = "worker")
    private Worker worker;

    @OneToOne
    @NonNull
    @JoinColumn(name = "task")
    private TodoTask task;

    @PrePersist
    public void prePersist() {
        if (task.getId() != null && worker.getId() != null) {
            compositeId = (task.getId() * 1_000_000) + worker.getId();
        }
    }
}

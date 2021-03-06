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
    @Column(name = "compos_id")
    private Long compositeId;

    @Column(name = "in_charge")
    private boolean responsible;

    @ManyToOne
    @NonNull
    @JoinColumn(name = "worker")
    private Worker worker;

    @ManyToOne
    @NonNull
    @JoinColumn(name = "task")
    private TodoTask task;

    @PrePersist
    public void prePersist() {
        if (task.getId() != null && worker.getId() != null) {
            compositeId = (task.getId() * 10_000_000) + worker.getId();
        }
    }
}

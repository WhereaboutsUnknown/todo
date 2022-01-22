package com.sagansar.todo.model.work;

import com.sagansar.todo.model.worker.Worker;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "worker_archived_task")
public class WorkerArchivedTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "in_charge")
    private boolean responsible;

    @OneToOne
    @JoinColumn(name = "worker", nullable = false)
    private Worker worker;

    @OneToOne
    @JoinColumn(name = "task", nullable = false)
    private ArchivedTask task;

    @Column(name = "estimate")
    private Integer estimate;

    public void setEstimate(Integer estimate) {
        if (estimate != null && (estimate < 0 || estimate > 5)) {
            throw new IllegalArgumentException("Оценка должна быть в диапазоне от 0 до 5");
        }
        this.estimate = estimate;
    }
}

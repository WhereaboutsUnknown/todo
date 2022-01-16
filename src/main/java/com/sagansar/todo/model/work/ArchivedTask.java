package com.sagansar.todo.model.work;

import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.model.worker.Worker;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "archived_tasks")
public class ArchivedTask implements TaskTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "header")
    private String header;

    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private Manager creator;

    @OneToOne
    @JoinColumn(name = "archived_by", nullable = false)
    private Manager archivedBy;

    @OneToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "main_stack")
    private String stack;

    @Column(name = "created", nullable = false)
    private LocalDateTime creationTime;

    @Column(name = "archived_since", nullable = false)
    private LocalDateTime archiveTime;

    @Column(name = "successful")
    private boolean successful;

    @Override
    public LocalDateTime getDeadline() {
        return null;
    }
}

package com.sagansar.todo.kanban.model;

import com.sagansar.todo.model.worker.Worker;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity(name = "kanban_ticket")
public class KanbanTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ticket_name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "finished")
    private LocalDateTime finishTime;

    @ManyToOne
    @JoinColumn(name = "worker")
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "creator")
    private Worker creator;

    @ManyToOne
    @JoinColumn(name = "column")
    private KanbanColumn column;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanbanComment> comments;
}

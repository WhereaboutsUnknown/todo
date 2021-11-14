package com.sagansar.todo.model.work;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
public class Dialog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "log_dir_path")
    private String logDirectory;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private TodoTask task;

    @OneToMany(orphanRemoval = true, mappedBy = "dialog", cascade = CascadeType.ALL)
    private Set<Message> messages;
}

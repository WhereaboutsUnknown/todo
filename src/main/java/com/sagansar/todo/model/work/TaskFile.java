package com.sagansar.todo.model.work;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "task_file")
public class TaskFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_name")
    private String name;

    @Column(name = "file_size")
    private Long size;

    @Column(name = "in_use")
    private boolean inUse;

    @Column(name = "deleted")
    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "task")
    private TodoTask task;
}

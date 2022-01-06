package com.sagansar.todo.model.work;

import com.sagansar.todo.model.general.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "load_date")
    private LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "task")
    private TodoTask task;

    @ManyToOne
    @JoinColumn(name = "load_by")
    private User creator;
}

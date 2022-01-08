package com.sagansar.todo.model.general;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public class AbstractFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Long id;

    @Column(name = "file_name")
    protected String name;

    @Column(name = "file_size")
    protected Long size;

    @Column(name = "in_use")
    protected boolean inUse;

    @Column(name = "deleted")
    protected boolean deleted;

    @Column(name = "load_date")
    protected LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "load_by")
    protected User creator;
}

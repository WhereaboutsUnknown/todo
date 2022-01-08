package com.sagansar.todo.model.work;

import com.sagansar.todo.model.general.AbstractFile;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "task_file")
public class TaskFile extends AbstractFile {

    @ManyToOne
    @JoinColumn(name = "task")
    protected TodoTask task;
}

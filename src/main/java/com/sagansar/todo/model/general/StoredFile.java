package com.sagansar.todo.model.general;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity(name = "stored_file")
public class StoredFile extends AbstractFile {
}

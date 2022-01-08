package com.sagansar.todo.repository;

import com.sagansar.todo.model.general.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
}

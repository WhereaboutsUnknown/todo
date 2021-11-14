package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.Dialog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DialogRepository extends JpaRepository<Dialog, Long> {
}

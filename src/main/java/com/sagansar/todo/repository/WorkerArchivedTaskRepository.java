package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.WorkerArchivedTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerArchivedTaskRepository extends JpaRepository<WorkerArchivedTask, Long> {

    List<WorkerArchivedTask> findAllByWorkerId(Integer workerId);
}

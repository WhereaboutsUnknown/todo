package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.TodoTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoTaskRepository extends JpaRepository<TodoTask, Long>, JpaSpecificationExecutor<TodoTask> {

    List<TodoTask> findAllByWorkerId(Integer workerId);

    List<TodoTask> findAllByManagerId(Integer managerId);

    @Query("SELECT task FROM todo_task task JOIN task.status WHERE (task.status.statusName = 'TODO' OR task.status.statusName = 'DISCUSSION')" +
            "AND task.visibleToAll = true")
    List<TodoTask> findAllAvailable();

    List<TodoTask> findAllByUnitId(Integer unitId);
}

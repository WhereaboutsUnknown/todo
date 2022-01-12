package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InviteRepository extends JpaRepository<Invite, Long> {

    List<Invite> findAllByTaskId(Long taskId);

    List<Invite> findAllByWorkerId(Integer workerId);

    boolean existsByWorkerIdAndTaskId(Integer workerId, Long taskId);
}

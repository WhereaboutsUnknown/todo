package com.sagansar.todo.repository;

import com.sagansar.todo.model.worker.WorkerResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkerResponseRepository extends JpaRepository<WorkerResponse, Long> {

    List<WorkerResponse> findAllByTaskIdAndCheckedFalse(Long taskId, Pageable pageable);

    @Query("SELECT wr FROM worker_response wr JOIN wr.task wrt JOIN wr.worker wrw WHERE wr.id = :taskId " +
            "AND wr.checked = false " +
            "AND (UPPER(wrt.header) LIKE UPPER(:request) OR UPPER(wrt.stack) LIKE UPPER(:request) OR UPPER(wrw.name) LIKE UPPER(:request) OR UPPER(wrw.info) LIKE UPPER(:request))")
    List<WorkerResponse> findAllByRequest(Long taskId, Pageable pageable, String request);

    boolean existsByWorkerIdAndTaskId(Integer workerId, Long taskId);

    List<WorkerResponse> findAllByResponseTimeBefore(LocalDateTime time);
}

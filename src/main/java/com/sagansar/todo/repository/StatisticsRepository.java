package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    Statistics findDistinctByWorkerId(Integer workerId);

    List<Statistics> findAllByWorkerIdIn(List<Integer> ids);
}

package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserId(Integer userId, Sort sort);

    void deleteAllByReadTimeNotNullAndReadTimeBefore(LocalDateTime time);
}

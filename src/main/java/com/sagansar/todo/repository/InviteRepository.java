package com.sagansar.todo.repository;

import com.sagansar.todo.model.manager.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRepository extends JpaRepository<Invite, Long> {
}

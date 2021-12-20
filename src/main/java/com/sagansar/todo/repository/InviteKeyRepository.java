package com.sagansar.todo.repository;

import com.sagansar.todo.model.work.InviteKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteKeyRepository extends JpaRepository<InviteKey, Long> {
    InviteKey findDistinctByKey(String key);
}

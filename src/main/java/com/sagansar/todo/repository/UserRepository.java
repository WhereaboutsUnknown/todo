package com.sagansar.todo.repository;

import com.sagansar.todo.model.general.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);
}

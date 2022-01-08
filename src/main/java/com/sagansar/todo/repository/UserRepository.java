package com.sagansar.todo.repository;

import com.sagansar.todo.model.general.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndActiveTrue(String username);
}

package com.sagansar.todo.repository;

import com.sagansar.todo.model.general.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByName(String name);
}

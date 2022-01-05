package com.sagansar.todo.repository;

import com.sagansar.todo.model.general.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactsRepository extends JpaRepository<Contacts, Long> {
}

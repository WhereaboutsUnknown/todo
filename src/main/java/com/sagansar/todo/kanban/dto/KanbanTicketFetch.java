package com.sagansar.todo.kanban.dto;

import com.sagansar.todo.controller.dto.PersonNameDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class KanbanTicketFetch {

    private Long id;

    private String name;

    private LocalDateTime deadline;

    private PersonNameDto worker;
}

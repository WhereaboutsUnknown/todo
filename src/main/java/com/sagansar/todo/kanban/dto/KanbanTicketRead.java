package com.sagansar.todo.kanban.dto;

import com.sagansar.todo.controller.dto.PersonNameDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class KanbanTicketRead {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime deadline;

    private LocalDateTime creationDate;

    private LocalDateTime finishTime;

    private PersonNameDto worker;

    private PersonNameDto creator;
}

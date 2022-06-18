package com.sagansar.todo.kanban.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class KanbanTicketEdit {

    private String name;

    private String description;

    private LocalDateTime deadline;
}

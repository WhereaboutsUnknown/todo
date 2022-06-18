package com.sagansar.todo.kanban.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class KanbanColumnRead {

    private Long id;

    private String name;

    private Integer order;

    private Boolean deletable;

    private Boolean finishing;

    private List<KanbanTicketFetch> tickets;
}

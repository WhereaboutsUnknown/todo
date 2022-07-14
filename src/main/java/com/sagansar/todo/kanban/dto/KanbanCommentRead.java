package com.sagansar.todo.kanban.dto;

import com.sagansar.todo.controller.dto.PersonNameDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class KanbanCommentRead {

    private Long id;

    private String text;

    private LocalDateTime time;

    private PersonNameDto author;

    private Boolean isOwn;
}

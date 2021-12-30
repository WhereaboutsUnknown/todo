package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkerResponseDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String message;

    @JsonProperty
    private LocalDateTime creationTime;

    @JsonProperty
    private WorkerFullDto worker;

    @JsonProperty
    private TaskShortDto task;
}

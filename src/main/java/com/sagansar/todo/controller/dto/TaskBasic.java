package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskBasic {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String header;

    @JsonProperty
    private String status;

    @JsonProperty
    private String stack;

    @JsonProperty
    private LocalDateTime deadline;

    @JsonProperty
    private String description;
}

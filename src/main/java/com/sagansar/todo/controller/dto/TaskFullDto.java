package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Extended task view for showing on task opening
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskFullDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String header;

    @JsonProperty
    private String description;

    @JsonProperty
    private PersonNameDto creator;

    @JsonProperty
    private PersonNameDto manager;

    @JsonProperty
    private PersonNameDto worker;

    @JsonProperty
    private Integer unitId;

    @JsonProperty
    private String status;

    @JsonProperty
    private String stack;

    @JsonProperty
    private LocalDateTime creationTime;

    @JsonProperty
    private LocalDateTime deadline;

    @JsonProperty
    private List<PersonNameDto> group;
}

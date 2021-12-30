package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkerResponseBasic {

    @JsonProperty
    private Long id;

    @JsonProperty
    private LocalDateTime creationTime;

    @JsonProperty
    private String task;

    @JsonProperty
    private String worker;
}

package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InviteDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Long taskId;

    @JsonProperty
    private String task;

    @JsonProperty
    private Integer workerId;

    @JsonProperty
    private boolean checked;

    @JsonProperty
    private boolean accepted;

    @JsonProperty
    private LocalDateTime creationTime;
}

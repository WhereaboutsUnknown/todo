package com.sagansar.todo.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sagansar.todo.model.work.TaskTemplate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskForm implements TaskTemplate {

    @JsonProperty
    private String header;

    @JsonProperty
    private String description;

    @JsonProperty
    private String stack;

    @JsonProperty
    private LocalDateTime deadline;

    @JsonProperty
    private LocalDateTime plannedStart;
}

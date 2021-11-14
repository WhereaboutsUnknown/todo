package com.sagansar.todo.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskForm {

    @JsonProperty
    private String header;

    @JsonProperty
    private String description;

    @JsonProperty
    @NonNull
    private Integer creatorId;

    @JsonProperty
    private Integer unitId;

    @JsonProperty
    private String stack;

    @JsonProperty
    private LocalDateTime deadline;
}

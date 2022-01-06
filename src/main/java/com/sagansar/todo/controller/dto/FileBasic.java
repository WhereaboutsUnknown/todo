package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileBasic {

    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private Long size;

    @JsonProperty
    private boolean video;
}

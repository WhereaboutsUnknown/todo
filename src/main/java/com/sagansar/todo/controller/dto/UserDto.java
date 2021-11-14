package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    @JsonProperty
    protected String username;

    @JsonProperty
    protected String firstName;

    @JsonProperty
    protected String patronym;

    @JsonProperty
    protected String surname;

    @JsonProperty
    protected String role;

    @JsonProperty
    protected String age;
}
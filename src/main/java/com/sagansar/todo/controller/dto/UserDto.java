package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    @JsonProperty
    protected  Integer id;

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
    protected LocalDate birthDate;

    @JsonProperty
    protected boolean active;

    @JsonProperty
    protected LocalDateTime registrationDate;
}

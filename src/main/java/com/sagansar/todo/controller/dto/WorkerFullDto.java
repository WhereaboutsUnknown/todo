package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkerFullDto {

    @JsonProperty
    private Integer id;

    @JsonProperty
    private String name;

    @JsonProperty
    protected String firstName;

    @JsonProperty
    protected String patronym;

    @JsonProperty
    protected String surname;

    @JsonProperty
    protected LocalDate birthDate;

    @JsonProperty
    protected String age;

    @JsonProperty
    private String skills;

    @JsonProperty
    private ContactsDto contacts;

    @JsonProperty
    private StatisticsBasic statistics;
}
package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactsDto {

    @JsonProperty
    private String phoneNumber;

    @JsonProperty
    private String email;

    @JsonProperty
    private String vk;

    @JsonProperty
    private String telegram;

    @JsonProperty
    private String facebook;

    @JsonProperty
    private String other;
}

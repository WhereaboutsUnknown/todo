package com.sagansar.todo.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkerProfileForm {

    @JsonProperty
    private String profileName;

    @JsonProperty
    protected String firstName;

    @JsonProperty
    protected String patronym;

    @JsonProperty
    protected String surname;

    @JsonProperty
    private String birthDate;

    @JsonProperty
    private String skills;

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

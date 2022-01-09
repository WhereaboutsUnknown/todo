package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsBasic {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Double points;

    @JsonProperty
    private Integer done;

    @JsonProperty
    private Integer failed;

    @JsonProperty
    private Double doneShare;

    @JsonProperty
    private Double failedShare;
}

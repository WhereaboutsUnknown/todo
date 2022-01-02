package com.sagansar.todo.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsFull {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Double points;

    @JsonProperty
    private Integer done;

    @JsonProperty
    private Integer failed;

    @JsonProperty
    private Integer doneShare;

    @JsonProperty
    private Integer failedShare;

    @JsonProperty
    private Integer rejected;

    @JsonProperty
    private Integer rejectedShare;

    @JsonProperty
    private Integer responsible;
}

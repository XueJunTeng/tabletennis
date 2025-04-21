package com.example.tabletennis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentTypeDTO {
    @JsonProperty("name")
    private String contentType;

    @JsonProperty("value")
    private Integer count;
}
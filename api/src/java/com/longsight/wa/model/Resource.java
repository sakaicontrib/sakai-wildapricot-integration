package com.longsight.wa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Url")
    private String url;
    @JsonProperty("AllowedOperations")
    private List<String> allowedOperations;
}

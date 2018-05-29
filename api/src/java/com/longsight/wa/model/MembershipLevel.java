package com.longsight.wa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MembershipLevel {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Url")
    private String url;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("PublicCanApply")
    private boolean publicCanApply;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("MembershipFee")
    private int membershipFee;
}

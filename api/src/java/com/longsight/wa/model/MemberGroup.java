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
public class MemberGroup {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Url")
    private String url;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("ContactsCount")
    private int contactsCount;
    @JsonProperty("ContactIds")
    private List<String> contactIds;    
}
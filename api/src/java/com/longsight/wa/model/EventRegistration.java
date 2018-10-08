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
public class EventRegistration {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Url")
    private String url;
    @JsonProperty("Contact")
    private Contact contact;
    @JsonProperty("RegistrationDate")
    private String registrationDate;
}

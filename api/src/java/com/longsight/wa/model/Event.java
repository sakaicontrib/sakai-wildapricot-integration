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
public class Event {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Url")
    private String url;
    @JsonProperty("EventType")
    private String eventType;
    @JsonProperty("StartDate")
    private String startDate;
    @JsonProperty("EndDate")
    private String endDate;
    @JsonProperty("Location")
    private String location;
    @JsonProperty("RegistrationEnabled")
    private boolean registrationEnabled;
    @JsonProperty("RegistrationsLimit")
    private int registrationsLimit;
    @JsonProperty("ConfirmedRegistrationsCount")
    private int confirmedRegistrationsCount;
    @JsonProperty("EndTimeSpecified")
    private boolean endTimeSpecified;
    @JsonProperty("Name")
    private String name;
}

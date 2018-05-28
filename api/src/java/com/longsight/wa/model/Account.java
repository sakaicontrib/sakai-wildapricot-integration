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
public class Account {
	@JsonProperty("Id")
	private String id;
	@JsonProperty("Url")
	private String url;
	@JsonProperty("PrimaryDomainName")
	private String primaryDomainName;
	@JsonProperty("Resources")
	private List<Resource> resources;
	@JsonProperty("Name")
	private String name;
}

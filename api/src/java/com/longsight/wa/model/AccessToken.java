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
public class AccessToken {
	private String access_token;
	private String token_type;
	private int expires_in;
	private String refresh_token;
	@JsonProperty("Permissions")
	private List<Permissions> permissions;
}
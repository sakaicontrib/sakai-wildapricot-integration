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
public class Contact {
	@JsonProperty("FirstName")
	private String firstName;
	@JsonProperty("LastName")
	private String lastName;
	@JsonProperty("Email")
	private String email;
	@JsonProperty("Organization")
	private String organization;
	@JsonProperty("Id")
	private String id;
	@JsonProperty("Url")
	private String url;
	@JsonProperty("IsAccountAdministrator")
	private boolean isAccountAdministrator;
	@JsonProperty("TermsOfUseAccepted")
	private boolean termsOfUseAccepted;
	@JsonProperty("ProfileLastUpdated")
	private String profileLastUpdated;
	@JsonProperty("MembershipLevel")
	private MembershipLevel membershipLevel;
	@JsonProperty("Status")
	private String status;
}

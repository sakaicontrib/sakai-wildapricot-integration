package com.longsight.wa.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientConfig;

import com.longsight.wa.model.MembershipLevel;

@NoArgsConstructor
@Slf4j
public class MembershipProxy {
	
    public List<MembershipLevel> getMembershipLevels(String authToken, String accountId) {
		ClientConfig config = new ClientConfig();

		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/Accounts/%s/MembershipLevels", accountId));
		Response response = target.request().
				accept(MediaType.APPLICATION_FORM_URLENCODED).
		        header("Authorization", "Bearer " + authToken).                
		        get();
		
		String responseString = response.readEntity(String.class);
		
		if(response.getStatus() == 200) {
			try {
				MembershipLevel[] membershipLevels = new ObjectMapper().readValue(responseString, MembershipLevel[].class);
				return Arrays.asList(membershipLevels);

			} catch (Exception e) {
				log.error("Error mapping the response into an List<MembershipLevel> {}", e);
			}
		}else {
			log.error("Error code: {} {}", response.getStatus(), responseString);
		}
		return new ArrayList<MembershipLevel>();
	}
    
    public MembershipLevel getMembershipLevel(String authToken, String accountId, String membershipLevelId) {
		ClientConfig config = new ClientConfig();

		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/Accounts/%s/MembershipLevels/%s" , accountId, membershipLevelId));
		Response response = target.request().
				accept(MediaType.APPLICATION_FORM_URLENCODED).
		        header("Authorization", "Bearer " + authToken).                
		        get();
		
		String responseString = response.readEntity(String.class);
		
		if(response.getStatus() == 200) {
			try {
				MembershipLevel membershipLevel = new ObjectMapper().readValue(responseString, MembershipLevel.class);
				return membershipLevel;

			} catch (Exception e) {
				log.error("Error mapping the response into an List<MembershipLevel> {}", e);
			}
		}else {
			log.error("Error code: {} {}", response.getStatus(), responseString);
		}
		return null;
	}
}

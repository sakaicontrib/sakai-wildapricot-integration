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

import com.longsight.wa.model.MemberGroup;

@NoArgsConstructor
@Slf4j
public class MemberGroupProxy {
    
    public List<MemberGroup> getMemberGroups(String authToken, String accountId) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/Accounts/%s/MemberGroups" ,accountId));
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Bearer " + authToken).                
                get();
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
                MemberGroup[] memberGroups = new ObjectMapper().readValue(responseString, MemberGroup[].class);
                return Arrays.asList(memberGroups);

            } catch (Exception e) {
                log.error("Error mapping the response into a List<MemberGroup> {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return new ArrayList<MemberGroup>();
    }
    
    public MemberGroup getMemberGroup(String authToken, String accountId, String memberGroupId) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/Accounts/%s/MemberGroups/%s" ,accountId, memberGroupId));
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Bearer " + authToken).                
                get();
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
                MemberGroup memberGroup = new ObjectMapper().readValue(responseString, MemberGroup.class);
                return memberGroup;

            } catch (Exception e) {
                log.error("Error mapping the response into a MemberGroup {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return null;
    }
}

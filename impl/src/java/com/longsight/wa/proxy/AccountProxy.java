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
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientConfig;

import com.longsight.wa.model.Account;

@Slf4j
public class AccountProxy {
    
    public List<Account> getAccounts(String authToken) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target("https://api.wildapricot.org/v2.1/Accounts");
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Bearer " + authToken).                
                get();
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
                Account[] accounts = new ObjectMapper().readValue(responseString, Account[].class);
                return Arrays.asList(accounts);
            } catch (Exception e) {
                log.error("Error mapping the response into an List<Account> {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return new ArrayList<Account>();
    }

    public Account getAccount(String authToken, String accountId) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/Accounts/%s", accountId));
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Bearer " + authToken).                
                get();
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
                Account account = new ObjectMapper().readValue(responseString, Account.class);
                return account;
            } catch (Exception e) {
                log.error("Error mapping the response into an Account {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return null;
    }
}

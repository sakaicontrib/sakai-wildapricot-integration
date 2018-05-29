package com.longsight.wa.proxy;

import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientConfig;

import com.longsight.wa.model.AccessToken;

@NoArgsConstructor
@Slf4j
public class AuthenticationProxy {

    private static final String PROPERTY_API_KEY_TEXT = "APIKEY";
    private static final String TOKEN_REQUEST_BODY = "grant_type=client_credentials&scope=auto";
        
    public AccessToken getAuthenticationToken(String apiKey) {
        String authString = PROPERTY_API_KEY_TEXT + ":" + apiKey;
        String authStringEnc = Base64.getEncoder().encodeToString((authString.getBytes()));
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target("https://oauth.wildapricot.org/auth/token");
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Basic " + authStringEnc).                
                post(Entity.entity(TOKEN_REQUEST_BODY, MediaType.APPLICATION_FORM_URLENCODED));
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
                AccessToken at = new ObjectMapper().readValue(responseString, AccessToken.class);                
                return at;
            } catch (Exception e) {
                log.error("Error mapping the response into an AccessToken {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return null;        
    }
    
    public boolean expireAuthenticationToken(String apiKey, String authenticationToken) {
        String authString = PROPERTY_API_KEY_TEXT + ":" + apiKey;
        String authStringEnc = Base64.getEncoder().encodeToString((authString.getBytes()));
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(String.format("https://oauth.wildapricot.org/auth/expiretoken?token=%s",authenticationToken));
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Basic " + authStringEnc).
                get();
        
        if(response.getStatus() == 200) {
            try {
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;        
    }

}
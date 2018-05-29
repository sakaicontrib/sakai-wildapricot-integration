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
import org.json.JSONArray;
import org.json.JSONObject;

import com.longsight.wa.model.Contact;

@NoArgsConstructor
@Slf4j
public class ContactProxy {
    
    public List<Contact> getAllContactsFromAccount(String authToken, String accountId) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/Accounts/%s/Contacts?$async=false", accountId));
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Bearer " + authToken).                
                get();
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
                JSONObject contactsObject = new JSONObject(responseString);
                JSONArray contactsArray = contactsObject.getJSONArray("Contacts");                
                Contact[] contacts = new ObjectMapper().readValue(contactsArray.toString(), Contact[].class);
                return Arrays.asList(contacts);
            } catch (Exception e) {
                log.error("Error mapping the response into an List<Contact> {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return new ArrayList<Contact>();
    }
    
    public Contact getContactFromAccount(String authToken, String accountId, String contactId) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/Accounts/%s/Contacts/%s", accountId, contactId));
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Bearer " + authToken).                
                get();
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
                Contact contact = new ObjectMapper().readValue(responseString, Contact.class);
                return contact;
            } catch (Exception e) {
                log.error("Error mapping the response into an List<Contact> {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return null;
    }
}

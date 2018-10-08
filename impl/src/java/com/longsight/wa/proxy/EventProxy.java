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
import org.json.JSONObject;

import com.longsight.wa.model.Event;
import com.longsight.wa.model.EventRegistration;

@NoArgsConstructor
@Slf4j
public class EventProxy {
    
    public List<Event> getEventList(String authToken, String accountId) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/Accounts/%s/Events", accountId));
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Bearer " + authToken).                
                get();
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
            	JSONObject jsonObject = new JSONObject(responseString);
            	String jsonEventsObject = (String) jsonObject.get("Events").toString();
            	Event[] eventList = new ObjectMapper().readValue(jsonEventsObject, Event[].class);
                return Arrays.asList(eventList);

            } catch (Exception e) {
                log.error("Error mapping the response into an List<Event> {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return new ArrayList<Event>();
    }

    public List<EventRegistration> getEventRegistration(String authToken, String accountId, String eventId) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(String.format("https://api.wildapricot.org/v2.1/accounts/%s/eventregistrations?eventId=%s", accountId, eventId));
        Response response = target.request().
                accept(MediaType.APPLICATION_FORM_URLENCODED).
                header("Authorization", "Bearer " + authToken).                
                get();
        
        String responseString = response.readEntity(String.class);
        
        if(response.getStatus() == 200) {
            try {
            	EventRegistration[] EventRegistrationList = new ObjectMapper().readValue(responseString, EventRegistration[].class);
                return Arrays.asList(EventRegistrationList);

            } catch (Exception e) {
                log.error("Error mapping the response into an List<EventRegistration> {}", e);
            }
        }else {
            log.error("Error code: {} {}", response.getStatus(), responseString);
        }
        return new ArrayList<EventRegistration>();
    }
}

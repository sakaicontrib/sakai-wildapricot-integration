package com.longsight.wa.jobs;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.longsight.wa.logic.SakaiProxy;
import com.longsight.wa.logic.SakaiWAConstants;
import com.longsight.wa.jobs.utils.JobUtils;
import com.longsight.wa.model.AccessToken;
import com.longsight.wa.model.Account;
import com.longsight.wa.model.Contact;
import com.longsight.wa.model.Event;
import com.longsight.wa.model.EventRegistration;
import com.longsight.wa.proxy.AccountProxy;
import com.longsight.wa.proxy.AuthenticationProxy;
import com.longsight.wa.proxy.ContactProxy;
import com.longsight.wa.proxy.EventProxy;

/**
 * WA MemberGroups Sync job
 *
 * @author Miguel Pellicer (mpellicer@edf.global)
 *
 */
@Slf4j
public class WAEventSyncJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //Abort if there is another execution of the job
        if(JobUtils.isJobRunning(jobExecutionContext)) return;
        
        //Proxy services
        AuthenticationProxy authenticationProxy = new AuthenticationProxy();
        AccountProxy accountProxy = new AccountProxy();
        EventProxy eventProxy = new EventProxy();
        ContactProxy contactProxy = new ContactProxy();

        //Control variables
        int total = 0;
        long startExecutionTime = System.nanoTime();

        //Session variables
        String adminUserEid = sakaiProxy.getConfigParam(SakaiWAConstants.DEFAULT_ADMIN_PROPERTY, SakaiWAConstants.DEFAULT_ADMIN_PROPERTY_VALUE);
        String defaultUserRole = sakaiProxy.getConfigParam(SakaiWAConstants.DEFAULT_USERROLE_PROPERTY, SakaiWAConstants.DEFAULT_USERROLE_PROPERTY_VALUE);

        boolean sessionStablished = sakaiProxy.establishSession(adminUserEid);
        if(!sessionStablished){
            log.error("Fatal error: Unable to stablish a session to execute the job");
            return;
        }
        
        log.info("-------START Executing WAEventSyncJob-------");
        
        /*************************************************
         ********* Getting the auth token*****************
         *************************************************/
        SakaiWAConstants.WA_APIKEY =  sakaiProxy.getConfigParam(SakaiWAConstants.WA_APIKEY_PROPERTY, null);

        //Abort execution if the APIKEY is not set
        if(StringUtils.isEmpty(SakaiWAConstants.WA_APIKEY)) {
            log.error("The WildApricot APIKEY is not set, please set the {} property in your sakai.properties and read the readme.md file to configure the integration properly.", SakaiWAConstants.WA_APIKEY);
            return;
        }

        AccessToken accessToken = authenticationProxy.getAuthenticationToken(SakaiWAConstants.WA_APIKEY);
        //Abort execution if the job cannot get an authentication token.
        if(accessToken == null) {
            log.error("Unable to get an authentication token, please set the {} property in your sakai.properties and read the readme.md file to configure the integration properly.", SakaiWAConstants.WA_APIKEY);
            return;            
        }
        log.info("--WA authentication token retrieved successfully...");

        /*************************************************
         ********* Getting the accounts list**************
         *************************************************/
        log.info("--Getting the account list from WA...");
        List<Account> accountList = accountProxy.getAccounts(accessToken.getAccess_token());

        log.info("--Found {} accounts in WA.", accountList.size());

        for(Account account : accountList) {

            String accountId = account.getId();
            List<Event> accountEventList = eventProxy.getEventList(accessToken.getAccess_token(), accountId);
            log.info("----Found {} events for the account {}.", accountEventList.size(), accountId);

            for(Event event : accountEventList) {
            	String eventId = event.getId();
            	log.info("------Found event {} with id {}.", event.getName(), eventId);

            	List<EventRegistration> eventRegistrationList = eventProxy.getEventRegistration(accessToken.getAccess_token(), accountId, eventId);
            	log.info("------Found event {} registrations in the event id {}.", eventRegistrationList.size(), eventId);

            	//Grab the users that are registered in this particular event
            	ArrayList<String> usersList = new ArrayList<String>();
            	for(EventRegistration eventRegistration : eventRegistrationList) {
            		String contactId = eventRegistration.getContact().getId();
            		Contact contact = contactProxy.getContactFromAccount(accessToken.getAccess_token(), accountId, contactId);
            		if (contact != null && contact.getEmail() != null) usersList.add(contact.getEmail());
            	}

            	//Grab from Sakai the sites that belong to a particular event
            	List<String> siteList = sakaiProxy.getSitesForEvent(eventId);
            	log.info("------Found {} sites in Sakai for the event id {}.", siteList.size(), eventId);
            	for(String siteId : siteList) {
            		//Remove all the users from the site that are not in the event
            		List<String> currentSiteMembers = sakaiProxy.getSiteMembers(siteId, defaultUserRole);
            		for(String userEid : currentSiteMembers) {
            			if(!usersList.contains(userEid)) {
            				log.info("--------Removing user {} from the site {}.", userEid, siteId);
            				sakaiProxy.removeMemberFromSite(siteId, userEid);
            			}
            		}
            		
            		//Add all the users that are registered in the event to the site
            		for(String userEid : usersList) {
            			log.info("--------Adding user {} in the site {}.", userEid, siteId);
            			sakaiProxy.addMemberToSite(siteId, userEid, defaultUserRole, true);
            		}            		
            	}
            }
        }

        /*************************************************
         ********* Destroying the auth token *************
         *************************************************/
        authenticationProxy.expireAuthenticationToken(SakaiWAConstants.WA_APIKEY, accessToken.getAccess_token());
        /*
         * Logging the results of the execution 
         */
        log.info("--Total events processed {}",total);
        long endExecutionTime = System.nanoTime();
        log.info("--Job executed in {} seconds",((double) (endExecutionTime - startExecutionTime)/ 1000000000.0));
        log.info("-------END Executing WAEventSyncJob-------");

        //Invalidate the session
        sakaiProxy.invalidateCurrentSession();
    }

    @Setter
    private SakaiProxy sakaiProxy;
}

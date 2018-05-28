package com.longsight.wa.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.entity.api.ResourceProperties;

import com.longsight.wa.logic.SakaiProxy;
import com.longsight.wa.logic.SakaiWAConstants;
import com.longsight.wa.model.AccessToken;
import com.longsight.wa.model.Account;
import com.longsight.wa.model.Contact;
import com.longsight.wa.model.MembershipLevel;
import com.longsight.wa.proxy.AccountProxy;
import com.longsight.wa.proxy.AuthenticationProxy;
import com.longsight.wa.proxy.ContactProxy;
import com.longsight.wa.proxy.MemberGroupProxy;
import com.longsight.wa.proxy.MembershipProxy;

/**
 * WA Contacts Sync job
 *
 * @author Miguel Carro Pellicer (mcarro@entornosdeformacion.com)
 *
 */
@Slf4j
public class WAContactsSyncJob implements Job {

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		//Abort if there is another execution of the job
		if(this.isJobRunning(jobExecutionContext)) return;
		
    	//Proxy services
		AuthenticationProxy authenticationProxy = new AuthenticationProxy();
    	AccountProxy accountProxy = new AccountProxy();
    	ContactProxy contactProxy = new ContactProxy();
    	MembershipProxy membershipProxy = new MembershipProxy();
    	MemberGroupProxy memberGroupProxy = new MemberGroupProxy();
		
		//Default variables
    	String defaultUserType = sakaiProxy.getConfigParam(SakaiWAConstants.DEFAULT_USERTYPE_PROPERTY, SakaiWAConstants.DEFAULT_USERTYPE_PROPERTY_VALUE);
    	
    	//Control variables
		int total = 0;
		int successUsers = 0;
		int failedUsers = 0;
		long startExecutionTime = System.nanoTime();

		//Session variables
		String adminUserEid = sakaiProxy.getConfigParam(SakaiWAConstants.DEFAULT_ADMIN_PROPERTY, SakaiWAConstants.DEFAULT_ADMIN_PROPERTY_VALUE);
		boolean sessionStablished = sakaiProxy.establishSession(adminUserEid);
		if(!sessionStablished){
			log.error("Fatal error: Unable to stablish a session to execute the job");
			return;
		}
		
		log.info("-------START Executing WAContactsSyncJob Job-------");
		
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
		log.info("--Getting the contacts list from WA...");
    	List<Account> accountList = accountProxy.getAccounts(accessToken.getAccess_token());
    	
    	log.info("--Found {} accounts in WA.", accountList.size());
        	
    	for(Account account : accountList) {
    		String accountId = account.getId();
        	List<Contact> contactList = contactProxy.getAllContactsFromAccount(accessToken.getAccess_token(), accountId);
        	log.info("----Found {} contacts for the account {}.", contactList.size(), accountId);
        	for(Contact contact : contactList) {
        		log.info("------Found contact: {} ", contact);
        		total++;
        		boolean updateUserProperties = false;

        		if(sakaiProxy.userEidExists(contact.getEmail())) {
        			//UserEid exists, update it
        			log.info("--------Contact exists, attempting to update it: {} ", contact);
        			boolean updated = sakaiProxy.updateUser(contact.getEmail(), contact.getFirstName(), contact.getLastName(), contact.getEmail());
        			if(updated) {
        				log.info("--------Contact updated successfully: {} ",contact);
        				successUsers++;
        				updateUserProperties = true;
        			}else {
        				log.error("--------Error updating the contact: {} ",contact);
        				failedUsers++;
        			}
        		}else {
        			log.info("--------Contact doesn't exist, attempting to create it: {} ", contact);
        			//UserEid not exists, create it        			
        			boolean created = sakaiProxy.addUser(contact.getEmail(), contact.getFirstName(), contact.getLastName(), contact.getEmail(), "12345", defaultUserType);
        			if(created) {
        				log.info("--------Contact created successfully: {} ",contact);
        				successUsers++;
        				updateUserProperties = true;
        			}else {
        				log.error("--------Error creating the contact: {} ",contact);
        				failedUsers++;
        			}
        		}

        		//Update the user properties
        		if(updateUserProperties) {
        			Map <String, String> userProperties = new HashMap<String, String>();
        			if(StringUtils.isNotEmpty(contact.getOrganization())) {
        				userProperties.put(SakaiWAConstants.USER_ORGANIZATION_PROPERTY, contact.getOrganization());
        			}
        			if(contact.getMembershipLevel() != null && StringUtils.isNotEmpty(contact.getMembershipLevel().getName())) {
        				userProperties.put(SakaiWAConstants.USER_MEMBERSHIPLEVEL_PROPERTY, contact.getMembershipLevel().getName());
        			}
    				sakaiProxy.setUserProperties(contact.getEmail(), userProperties);
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
		log.info("--Total users processed {}",total);
		log.info("--Users processed successfully {}",successUsers);
		log.info("--Failed users {}",failedUsers);
		long endExecutionTime = System.nanoTime();
		log.info("--Job executed in {} seconds",((double) (endExecutionTime - startExecutionTime)/ 1000000000.0));
		log.info("-------END Executing WAContactsSyncJob Job-------");
		
		//Invalidate the session
		sakaiProxy.invalidateCurrentSession();
	}

	@Setter
	private SakaiProxy sakaiProxy;

	private boolean isJobRunning(JobExecutionContext jobExecutionContext) {
		try {
			List<JobExecutionContext> jobs = jobExecutionContext.getScheduler().getCurrentlyExecutingJobs();
			for (JobExecutionContext job : jobs) {
				if (job.getTrigger().equals(jobExecutionContext.getTrigger()) && !job.getJobInstance().equals(jobExecutionContext.getJobInstance())) {
					log.error("There's another instance running, so leaving" + this);
					return true;
				}

			}
		}catch(Exception ex){log.warn("WARNING : "+ex.getMessage());}
		return false;
	}
}

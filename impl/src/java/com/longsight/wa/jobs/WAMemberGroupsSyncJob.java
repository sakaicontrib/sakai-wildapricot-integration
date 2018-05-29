package com.longsight.wa.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.longsight.wa.jobs.utils.JobUtils;
import com.longsight.wa.logic.SakaiProxy;
import com.longsight.wa.logic.SakaiWAConstants;
import com.longsight.wa.model.AccessToken;
import com.longsight.wa.model.Account;
import com.longsight.wa.model.Contact;
import com.longsight.wa.model.MemberGroup;
import com.longsight.wa.proxy.AccountProxy;
import com.longsight.wa.proxy.AuthenticationProxy;
import com.longsight.wa.proxy.ContactProxy;
import com.longsight.wa.proxy.MemberGroupProxy;

/**
 * WA MemberGroups Sync job
 *
 * @author Miguel Pellicer (mpellicer@edf.global)
 *
 */
@Slf4j
public class WAMemberGroupsSyncJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //Abort if there is another execution of the job
        if(JobUtils.isJobRunning(jobExecutionContext)) return;
        
        //Proxy services
        AuthenticationProxy authenticationProxy = new AuthenticationProxy();
        AccountProxy accountProxy = new AccountProxy();
        ContactProxy contactProxy = new ContactProxy();
        MemberGroupProxy memberGroupProxy = new MemberGroupProxy();

        //Control variables
        int total = 0;
        long startExecutionTime = System.nanoTime();

        //Session variables
        String adminUserEid = sakaiProxy.getConfigParam(SakaiWAConstants.DEFAULT_ADMIN_PROPERTY, SakaiWAConstants.DEFAULT_ADMIN_PROPERTY_VALUE);
        boolean sessionStablished = sakaiProxy.establishSession(adminUserEid);
        if(!sessionStablished){
            log.error("Fatal error: Unable to stablish a session to execute the job");
            return;
        }
        
        Map<String, List<String>> userGroupsMap = new HashMap<String, List<String>>();
        
        log.info("-------START Executing WAMemberGroupsSyncJob-------");
        
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
            List<MemberGroup> memberGroups = memberGroupProxy.getMemberGroups(accessToken.getAccess_token(), accountId);
            log.info("----Found {} memberGroups for the account {}.", memberGroups.size(), accountId);
            for(MemberGroup memberGroup : memberGroups) {
                //Groups coming from the method getMemberGroups doesn't contain the contactIds
                MemberGroup completeMemberGroup = memberGroupProxy.getMemberGroup(accessToken.getAccess_token(), accountId, memberGroup.getId());
                log.info("------Found memberGroup: {}", completeMemberGroup);
                total++;

                //If the group is empty, just skip it.
                if(completeMemberGroup.getContactIds() == null) {
                    continue;
                }

                for(String contactId : completeMemberGroup.getContactIds()) {
                    List<String> currentGroupsList = userGroupsMap.get(contactId);

                    if(currentGroupsList == null) {
                        currentGroupsList = new ArrayList<String>();
                    }

                    currentGroupsList.add(completeMemberGroup.getId());
                    userGroupsMap.put(contactId, currentGroupsList);
                }
            }

            //Add the current groups to each user
            for(Entry<String, List<String>> userMap : userGroupsMap.entrySet()) {
                String contactId = userMap.getKey();
                List<String> memberGroupIds = userMap.getValue();
                Contact contact = contactProxy.getContactFromAccount(accessToken.getAccess_token(), accountId, contactId);
                if(contact !=null) {
                    Map <String, String> userProperties = new HashMap<String, String>();
                    String userEid = contact.getEmail();
                    log.info("------Assign to this user {} these groups {}", userEid, memberGroupIds);
                    userProperties.put(SakaiWAConstants.USER_MEMBERGROUPS_PROPERTY, String.join(",", memberGroupIds));
                    sakaiProxy.setUserProperties(userEid, userProperties);
                }
            }

            //Remove the groups for the contacts which doesn't belong to any group
            List<Contact> contactList = contactProxy.getAllContactsFromAccount(accessToken.getAccess_token(), accountId);
            for(Contact contact : contactList) {
                if (!userGroupsMap.containsKey(contact.getId())) {
                    Map <String, String> userProperties = new HashMap<String, String>();
                    String userEid = contact.getEmail();
                    log.info("------User {} doesn't have groups, removing the assigned groups", userEid);
                    userProperties.put(SakaiWAConstants.USER_MEMBERGROUPS_PROPERTY, "");
                    sakaiProxy.setUserProperties(userEid, userProperties);
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
        log.info("--Total groups processed {}",total);
        long endExecutionTime = System.nanoTime();
        log.info("--Job executed in {} seconds",((double) (endExecutionTime - startExecutionTime)/ 1000000000.0));
        log.info("-------END Executing WAMemberGroupsSyncJob-------");

        //Invalidate the session
        sakaiProxy.invalidateCurrentSession();
    }

    @Setter
    private SakaiProxy sakaiProxy;
}

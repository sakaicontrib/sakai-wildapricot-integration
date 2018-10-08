package com.longsight.wa.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.user.api.User;

import com.longsight.wa.jobs.utils.JobUtils;
import com.longsight.wa.logic.SakaiProxy;
import com.longsight.wa.logic.SakaiWAConstants;

/**
 * WA CourseMembership Sync job
 *
 * @author Miguel Pellicer (mpellicer@edf.global)
 *
 */
@Slf4j
public class WACourseMembershipSyncJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //Abort if there is another execution of the job
        if(JobUtils.isJobRunning(jobExecutionContext)) return;
        
        //Control variables
        int total = 0;
        long startExecutionTime = System.nanoTime();
        
        //Default variables
        String defaultUserRole = sakaiProxy.getConfigParam(SakaiWAConstants.DEFAULT_USERROLE_PROPERTY, SakaiWAConstants.DEFAULT_USERROLE_PROPERTY_VALUE);

        //Session variables
        String adminUserEid = sakaiProxy.getConfigParam(SakaiWAConstants.DEFAULT_ADMIN_PROPERTY, SakaiWAConstants.DEFAULT_ADMIN_PROPERTY_VALUE);
        boolean sessionStablished = sakaiProxy.establishSession(adminUserEid);
        if(!sessionStablished){
            log.error("Fatal error: Unable to stablish a session to execute the job");
            return;
        }
        
        log.info("-------START Executing WACourseMembershipSyncJob-------");
        /*************************************************
         ********* Getting the users list**************
         *************************************************/
        log.info("--Getting the users from Sakai...");
        List<User> userList = sakaiProxy.getUsers();

        log.info("--Found {} users in Sakai.", userList.size());

        for(User user : userList) {

        	//Don't process disabled users or users without properties.
        	if(user.getProperties() == null || !sakaiProxy.isUserEnabled(user.getEid())) {
        		continue;
        	}

        	//Get the WA membership level and the member groups.
        	String userMembershipLevel = user.getProperties().getProperty(SakaiWAConstants.WA_MEMBERSHIPLEVEL_PROPERTY);
        	String userMemberGroups = user.getProperties().getProperty(SakaiWAConstants.WA_MEMBERGROUPS_PROPERTY);

        	//Don't process users without a membership nor member groups.
        	if(StringUtils.isEmpty(userMembershipLevel) && StringUtils.isEmpty(userMemberGroups)) {
        		continue;
        	}

        	log.info("----Processing user {}.", user.getEid());
        	total++;
        	
        	List<String> sitesWithAccess = new ArrayList<String>();

        	//Add member to membership sites
        	if(StringUtils.isNotEmpty(userMembershipLevel)) {
        		log.info("------Processing user {} membership level {}.", user.getEid(), userMembershipLevel);
        		List<String> sites = sakaiProxy.getSitesForMembershipLevel(userMembershipLevel);
        		log.info("------Found {} sites with membership level {}.", sites.size(), userMembershipLevel);
        		for(String siteId : sites) {
        			sitesWithAccess.add(siteId);
        		}
        	}
        	
        	//Add member to member group sites
        	if(StringUtils.isNotEmpty(userMemberGroups)) {
        		log.info("------Processing user {} member groups {}.", user.getEid(), userMemberGroups);
        		List<String> userMemberGroupList = Arrays.asList(userMemberGroups.split("\\s*,\\s*"));
        		for(String memberGroup : userMemberGroupList) {
        			List<String> sites = sakaiProxy.getSitesForMemberGroup(memberGroup);
            		log.info("------Found {} sites with member group {}.", sites.size(), memberGroup);
            		for(String siteId : sites) {
            			sitesWithAccess.add(siteId);
            		}
        		}
        	}

        	//Add the user all the sites with access
        	for(String siteId : sitesWithAccess) {
        		sakaiProxy.addMemberToSite(siteId, user.getEid(), defaultUserRole, true);
        		log.info("------Added member {} to site {} with role {} .", user.getEid(), siteId, defaultUserRole);
        	}
        	
    		// Disable the user from old memberships
        	List<String> userSites = sakaiProxy.getSitesUserHasAccess(user.getId());
        	for(String siteId : userSites) {
        		if(!sitesWithAccess.contains(siteId)) {
        			List<String> membershipLevels = sakaiProxy.getSiteMembershipLevels(siteId);
        			List<String> memberGroups = sakaiProxy.getSiteMemberGroups(siteId);
        			if(!(membershipLevels.isEmpty() && memberGroups.isEmpty())) {
        				sakaiProxy.removeMemberFromSite(siteId, user.getEid());
        				sakaiProxy.addMemberToSite(siteId, user.getEid(), defaultUserRole, false);
        				log.info("------Disabled member {} to site {} with role {} .", user.getEid(), siteId, defaultUserRole);
        			}        			
        		}
        	}
        }

        /*
         * Logging the results of the execution 
         */
        log.info("--Total users processed {}",total);
        long endExecutionTime = System.nanoTime();
        log.info("--Job executed in {} seconds",((double) (endExecutionTime - startExecutionTime)/ 1000000000.0));
        log.info("-------END Executing WACourseMembershipSyncJob-------");

        //Invalidate the session
        sakaiProxy.invalidateCurrentSession();
    }

    @Setter
    private SakaiProxy sakaiProxy;
}

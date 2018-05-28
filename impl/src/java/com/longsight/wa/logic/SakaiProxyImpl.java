package com.longsight.wa.logic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Implementation of our SakaiProxyImpl API
 * 
 * @author Miguel Pellicer (mpellicer@edf.global)
 *
 */
@Slf4j
public class SakaiProxyImpl implements SakaiProxy {

	@Override
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	@Override
	public String getCurrentUserDisplayName() {
	   return userDirectoryService.getCurrentUser().getDisplayName();
	}

	@Override
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}

	@Override
	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
	}

	@Override
	public boolean getConfigParam(String param, boolean dflt) {
		return serverConfigurationService.getBoolean(param, dflt);
	}

	@Override
	public String getConfigParam(String param, String dflt) {
		return serverConfigurationService.getString(param, dflt);
	}

	@Override
	public String[] getConfigParam(String param){
		return serverConfigurationService.getStrings(param);
	}

	@Override
	public int getConfigParam(String param, int dflt){
		return serverConfigurationService.getInt(param, dflt);
	}

	@Override
	public boolean establishSession(String userEid){
		Session session = sessionManager.getCurrentSession();
		try{
			session.setUserId(userDirectoryService.getUserByEid(userEid).getId());
			session.setUserEid(userEid);
			return true;
		}catch(Exception ex){
			log.error(String.format("UcIntegrationSakaiProxy: Fatal error configuring a session for the user %s",userEid));
		}
		return false;
	}

	@Override
	public void invalidateCurrentSession(){
		Session session = sessionManager.getCurrentSession();
		session.invalidate();
	}

	@Override
	public boolean isValidSite(String siteId){
		return siteService.siteExists(siteId);
	}

	@Override
	public void sendEmailToUsers(String[] recipients, String subject, String content){
		Collection<User> userCollection = convertUserStringArrayToUserCollection(recipients);
		Collection<String> subjectCollection = new HashSet<String>();
		subjectCollection.add(subject);
		emailService.sendToUsers(userCollection, subjectCollection, content);
	}

	private Collection<User> convertUserStringArrayToUserCollection(String[] input){
		Collection<User> userCollection = new HashSet<User>();
		for(int i = 0; i<input.length ; i++){
			try {
				userCollection.add(userDirectoryService.getUserByEid(input[i]));
			} catch (UserNotDefinedException e) {
				continue;
			}
		}
		return userCollection;
	}

	@Override
	public String getServerName(){
		return serverConfigurationService.getServerName();
	}

	@Override
	public boolean addMemberToSite(String siteId, String userEid, String roleId, boolean active){
		try{
			Site siteEdit = null;
			User user = userDirectoryService.getUserByEid(userEid);
			siteEdit = siteService.getSite(siteId);
			siteEdit.addMember(user.getId(), roleId, active, false);
			siteService.saveSiteMembership(siteEdit);
			return true;
		}catch(Exception ex){
			log.error("addMemberToSite() Fatal error adding the user to the site "+ex);
		}
		return false;
	}

	@Override
	public boolean removeMemberFromSite(String siteId, String userEid){
		try{
			Site siteEdit = null;
			User user = userDirectoryService.getUserByEid(userEid);
			siteEdit = siteService.getSite(siteId);
			siteEdit.removeMember(user.getId());
			siteService.saveSiteMembership(siteEdit);
			return true;
		}catch(Exception ex){
			log.error("removeMemberFromSite() Fatal error removing the user from the site "+ex);
		}
		return false;
	}

	@Override
	public boolean userEidExists(String userEid){
		try{
			userDirectoryService.getUserByEid(userEid);
			return true;
		}catch(Exception ex){
		}
		return false;
	}

	@Override
	public boolean userIdExists(String userId){
		try{
			userDirectoryService.getUser(userId);
			return true;
		}catch(Exception ex){
		}
		return false;
	}

	@Override
	public boolean isValidRole(String siteId, String roleId){
		try{
			Site siteEdit = null;
			siteEdit = siteService.getSite(siteId);
			Role role = siteEdit.getRole(roleId);
			if(role!=null) return true;
		}catch(Exception ex){
			log.error("isValidRole() Fatal error, the role does not exist "+ex);
		}
		return false;
	}

	@Override
	public boolean addUser(String userEid, String firstName, String lastName, String email, String password, String type){
		try{
			userDirectoryService.addUser(null, userEid, firstName, lastName, email, password, type, null);
			return true;
		}catch(Exception ex){
			log.error(String.format("addUser: Error adding the user %s - %s", userEid,ex.toString()));
		}
		return false;
	}
	
	@Override
	public boolean updateUser(String userEid, String firstName, String lastName, String email){
		try{
			User user = userDirectoryService.getUserByEid(userEid);
			UserEdit userEdit = userDirectoryService.editUser(user.getId());

			userEdit.setEmail(email);
			userEdit.setFirstName(firstName);
			userEdit.setLastName(lastName);

			userDirectoryService.commitEdit(userEdit);
			return true;
		}catch(Exception ex){
			log.error("updateUser: Error editing user {} : {}", userEid, ex.toString());
		}
		return false;
	}

	@Override
	public String getUserIdFromEid(String userEid){
		try{
			return userDirectoryService.getUserByEid(userEid).getId();
		}catch(Exception ex){
			log.error(String.format("getUserIdFromEid: Error getting the userId %s - %s", userEid,ex.toString()));
		}
		return null;
 	}

	@Override
	public String getUserEidFromId(String userId){
		try{
			return userDirectoryService.getUser(userId).getEid();
		}catch(Exception ex){
			log.error(String.format("getUserEidFromId: Error getting the userId %s - %s", userId,ex.toString()));
		}
		return null;
 	}

	@Override
	public boolean setUserProperties(String userEid, Map<String, String> userProperties) {
		UserEdit editUser = null;
		try {
            User user = userDirectoryService.getUserByEid(userEid);
            editUser = userDirectoryService.editUser(user.getId());
            ResourceProperties props = editUser.getProperties();
            for(Entry<String, String> userProperty : userProperties.entrySet()) {
            	props.addProperty(userProperty.getKey(), userProperty.getValue());
            }            
            userDirectoryService.commitEdit(editUser);
        } catch (Exception e) {
        	userDirectoryService.cancelEdit(editUser);
            log.error("setUserProperties: Error setting the properties for the userEid {} - {}", userEid, e.toString());
        }
		return false;
	}
	
	@Override
	public boolean isUserEnabled(String userEid) {
		UserEdit editUser = null;
		try {
            User user = userDirectoryService.getUserByEid(userEid);
            editUser = userDirectoryService.editUser(user.getId());
            boolean status = editUser.getProperties().getBooleanProperty(SakaiWAConstants.SAKAI_STATUS_PROPERTY);
            userDirectoryService.cancelEdit(editUser);
            return status;
        } catch (Exception e) {
        	userDirectoryService.cancelEdit(editUser);
            log.error("setUserProperties: Error setting the properties for the userEid {} - {}", userEid, e.toString());
        }
		return false;		
	}

	@Override
	public boolean setUserStatus(String userEid, boolean enabled) {
		UserEdit editUser = null;
		try {
            User user = userDirectoryService.getUserByEid(userEid);
            editUser = userDirectoryService.editUser(user.getId());
            editUser.getProperties().removeProperty(SakaiWAConstants.SAKAI_STATUS_PROPERTY);
			if(!enabled){
				editUser.getProperties().addProperty(SakaiWAConstants.SAKAI_STATUS_PROPERTY, "true");
			}
            userDirectoryService.commitEdit(editUser);
            return true;
        } catch (Exception e) {
        	userDirectoryService.cancelEdit(editUser);
            log.error("setUserProperties: Error setting the properties for the userEid {} - {}", userEid, e.toString());
        }
		return false;		
	}

	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}

	@Getter @Setter
	private SessionManager sessionManager;

	@Getter @Setter
	private UserDirectoryService userDirectoryService;

	@Getter @Setter
	private SecurityService securityService;

	@Getter @Setter
	private EventTrackingService eventTrackingService;

	@Getter @Setter
	private ServerConfigurationService serverConfigurationService;

	@Getter @Setter
	private SiteService siteService;

	@Getter @Setter
	private EmailService emailService;
}
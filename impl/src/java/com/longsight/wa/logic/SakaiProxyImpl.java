package com.longsight.wa.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

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
            String status = editUser.getProperties().getProperty(SakaiWAConstants.SAKAI_STATUS_PROPERTY);
            userDirectoryService.cancelEdit(editUser);
            return "true".equals(status) ? false : true;
        } catch (Exception e) {
            userDirectoryService.cancelEdit(editUser);
            log.error("isUserEnabled: Error checking if the user is enabled {} - {}", userEid, e.toString());
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
            log.error("setUserStatus: Error setting the user status for the userEid {} - {}", userEid, e.toString());
        }
        return false;        
    }
    
    @Override
    public List<String> getSitesUserHasAccess(String userId) {
		//Get the site list using queries, filtering the user workspace and the archived sites
		String dbQuery = "select ss.site_id from SAKAI_SITE_USER ssu, SAKAI_SITE ss"
				+ " where ssu.site_id = ss.site_id and ssu.user_id = '%s' and ssu.site_id <> concat('~',ssu.user_id)"
				+ " and ss.published = 1"
				+ " and not exists(select * from SAKAI_PREFERENCES sp where sp.preferences_id = ssu.user_id and sp.xml like concat('%%name=\"exclude\" value=\"',TO_BASE64(ssu.site_id),'\"%%')) order by ss.title asc;";
		try{
			dbQuery = String.format(dbQuery, userId);
			return sqlService.dbRead(dbQuery);
		}catch(Exception ex){
			log.error("Fatal error getting the siteList "+ex);
		}
		return new ArrayList<String>();
    }
    
    @Override
    public List<String> getSiteMembershipLevels(String siteId) {
    	return getPropertyValueFromSite(SakaiWAConstants.USER_MEMBERSHIPLEVEL_PROPERTY, siteId);
    }

    @Override
    public List<String> getSiteMemberGroups(String siteId){
    	return getPropertyValueFromSite(SakaiWAConstants.USER_MEMBERGROUPS_PROPERTY, siteId);
    }
    
    private List<String> getPropertyValueFromSite(String property, String siteId){
    	return sqlService.dbRead(String.format(SakaiWAConstants.SQL_GET_PROPERTY_VALUE, property, siteId));
    }
    
    @Override
    public List<User> getUsers(){
    	return userDirectoryService.getUsers();
    }
    
    @Override
    public List<Site> getUserSites(String userId){
    	return siteService.getUserSites(false, userId);
    }
    
    @Override
    public List<String> getSitesForMembershipLevel(String membershipLevel){
    	return getSitesWithProperty(SakaiWAConstants.USER_MEMBERSHIPLEVEL_PROPERTY, membershipLevel);
    }

    @Override
    public List<String> getSitesForMemberGroup(String memberGroup){
    	return getSitesWithProperty(SakaiWAConstants.USER_MEMBERGROUPS_PROPERTY, memberGroup);
    }
    
    private List<String> getSitesWithProperty(String property, String value){
    	return sqlService.dbRead(String.format(SakaiWAConstants.SQL_GET_SITES_WITH_PROPERTY, property, value));
    }
    
    public void notifyNewUserEmail(String userEid) {
    	try {
    		validationLogic.createValidationAccount(userDirectoryService.getUserByEid(userEid).getId(), true);
		} catch (Exception e) {
			log.error("Fatal error sending the notification, user {} not exists", userEid);
		}
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
    private ValidationLogic validationLogic;
    
    @Getter @Setter
    private SqlService sqlService;
}

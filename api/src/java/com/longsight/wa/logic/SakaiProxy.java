package com.longsight.wa.logic;

import java.util.Map;

/**
 * An interface to abstract all Sakai related API calls in a central method that can be injected into our app.
 * 
 * @author Miguel Pellicer (mpellicer@edf.global)
 *
 */
public interface SakaiProxy {

	/**
	 * Get current user id
	 * @return
	 */
	public String getCurrentUserId();
	
	/**
	 * Get current user display name
	 * @return
	 */
	public String getCurrentUserDisplayName();
	
	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 * @return
	 */
	public boolean isSuperUser();
	
	/**
	 * Post an event to Sakai
	 * 
	 * @param event			name of event
	 * @param reference		reference
	 * @param modify		true if something changed, false if just access
	 * 
	 */
	public void postEvent(String event,String reference,boolean modify);
		
	/**
	 * Get a configuration parameter as a boolean
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public boolean getConfigParam(String param, boolean dflt);
	
	/**
	 * Get a configuration parameter as a String
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public String getConfigParam(String param, String dflt);
	
	/**
	 * Get a configuration parameter as a String[]
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public String[] getConfigParam(String param);
	
	/**
	 * Get a configuration parameter as a integer
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public int getConfigParam(String param, int dflt);
	
	/**
	 * Establish a session for userEid (Important to make some operations)
	 * 
	 * @param	userEid will be the owner of the session 
	 * @return
	 */
	public boolean establishSession(String userEid);
	
	/**
	 * Invalidates the current session
	 * 
	 * @param	userEid will be the owner of the session 
	 * @return
	 */
	public void invalidateCurrentSession();
	
	/**
	 * Checks if the SiteId is valid
	 * @return
	 */
	public boolean isValidSite(String siteId);
	
	/**
	 * Send email to users
	 * @return
	 */
	public void sendEmailToUsers(String[] recipients, String subject, String content);
	
	/**
	 * Get the server name
	 * @return
	 */
	public String getServerName();
	
	/**
	 * Adds a member to the site
	 * @return
	 */
	public boolean addMemberToSite(String siteId, String userEid, String roleId, boolean active);
	
	/**
	 * Removes a member from the site
	 * @return
	 */
	public boolean removeMemberFromSite(String siteId, String userEid);
	
	/**
	 * Checks if the userEid exists
	 * @return
	 */
	public boolean userEidExists(String userEid);
	
	/**
	 * Checks if the userId exists
	 * @return
	 */
	public boolean userIdExists(String userId);
	
	/**
	 * Checks if the roleId is valid in the siteId
	 * @return
	 */
	public boolean isValidRole(String siteId, String roleId);
	
	/**
	 * Adds a new user
	 * @return true if was added successfully, false if failed
	 */
	public boolean addUser(String userEid, String firstName, String lastName, String email, String password, String type);

	/**
	 * Edits an existing user
	 * @return true if was edited successfully, false if failed
	 */
	public boolean updateUser(String userEid, String firstName, String lastName, String email);
	
	/**
	 * Gets the user status
	 * @return true if the user is enabled, false otherwise
	 */
	public boolean isUserEnabled(String userEid);

	/**
	 * Sets the user status to enabled/disabled
	 * @return true if was edited successfully, false if failed
	 */
	public boolean setUserStatus(String userEid, boolean enabled);
	
	/**
	 * Gets the userId from the Eid.
	 * @return userId as string, null if not exists
	 */
	public String getUserIdFromEid(String userEid);
	
	/**
	 * Gets the userEid from the Id.
	 * @return userEid as string, null if not exists
	 */
	public String getUserEidFromId(String userId);

	/**
	 * Sets some user properties
	 * @return userEid as string, null if not exists
	 */
	public boolean setUserProperties(String userEid, Map<String, String> userProperties);
}

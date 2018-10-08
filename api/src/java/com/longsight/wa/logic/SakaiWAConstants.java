package com.longsight.wa.logic;

public class SakaiWAConstants {
    public static final String DEFAULT_ADMIN_PROPERTY = "wa.sync.admin";
    public static final String DEFAULT_ADMIN_PROPERTY_VALUE = "admin";
    
    public static final String WA_APIKEY_PROPERTY = "wa.sync.apikey";
    public static String WA_APIKEY = null;
    
    public static final String DEFAULT_USERTYPE_PROPERTY = "wa.sync.default.usertype";
    public static final String DEFAULT_USERTYPE_PROPERTY_VALUE = "registered";
    
    public static final String DEFAULT_USERROLE_PROPERTY = "wa.sync.default.role";
    public static final String DEFAULT_USERROLE_PROPERTY_VALUE = "access";
    
    public static final String WA_ORGANIZATION_PROPERTY = "wa-organization";
    public static final String WA_MEMBERSHIPLEVEL_PROPERTY = "wa-membershiplevel";
    public static final String WA_MEMBERGROUPS_PROPERTY = "wa-membergroups";
    public static final String WA_EVENTS_PROPERTY = "wa-events";
    
    public static final String WA_ACTIVE_STATUS = "Active";
    
    public static final String SAKAI_STATUS_PROPERTY = "disabled";
    
    public static final String SQL_GET_SITES_WITH_PROPERTY = "select site_id from sakai_site_property where name='%s' and value like '%%%s%%';";
    
    public static final String SQL_GET_PROPERTY_VALUE = "select value from sakai_site_property where name='%s' and site_id='%s';";
}

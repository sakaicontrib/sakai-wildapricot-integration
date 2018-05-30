# Sakai Wildapricot Integration
This software component integrates the Wild Apricot software with Sakai.
[![WildApricot](https://www.memberpath.com/wp-content/uploads/2017/12/Wild-Apricot.png)](https://www.wildapricot.com/)

This integration was done using **Sakai 13-SNAPSHOT** and **Wild Apricot 6.2.0.209 with [API v2.1](https://gethelp.wildapricot.com/en/articles/1599-api-version-21-differences)**. Future versions of Wild Apricot (WA) or Sakai may break this integration and may require an integration upgrade, check Sakai's and WA's API documentation to get more information.

## Use cases
WA has many features like the website builder, membership manager, member group manager and eCommerce solution, this integration supports the following use cases:

### Contacts sync from all accounts
All the WA contacts from all the accounts are retrieved and synchronized with Sakai. Sakai queries the WA server periodically, retrieves the list of contacts and creates them.
If the contact already exists, Sakai updates its data including firstname, surname and email.


Sakai updates three WA parameters in every synchronization:
* User status: If the member is not active in WA for some reason, Sakai disables it. If the member is active in WA, Sakai enables it.
* Membership Level. Sakai stores the membership level identifier of the contact.
* Organization. Sakai stores the contact's organization.

Member groups are more complex and are synchronized separately.

### Member groups sync from all accounts
All the member groups from all accounts are retrieved and synchronized with Sakai. Sakai queries the WA server periodically, retrieves the list of the groups, the list of the contacts of every group, and stores the information in every user.

After getting all the groups of every contact, Sakai stores the updated list of groups in every user.
If a user doesn't belong to any group, the current groups of the user will be removed.

### Course Membership sync
 
Due to the two first use cases, all the Sakai users have updated data, including the membership level and the member groups to which they belong.

Sakai process every user and grants access to the content which the user has access too, including courses or sites which belongs to a membership level or a member group.

## Technical documentation
### Integration configuration
Set the following properties in the **sakai.properties** file:
```
# User with admin privileges which performs the operations in Sakai
wa.sync.admin= admin
# WA API Key *
wa.sync.apikey=1234567890
# Default user type assigned to the user when created
wa.sync.default.usertype=registered
# Default user role assigned to the user when enrolled in a course
wa.sync.default.role=access
```
*APIKEY, to integrate WA with Sakai successfully, you should create an authorized application and grant access to Sakai in the WA administration panel. Go to Settings -> Security -> Authorized applications. Get an API KEY to authenticate Sakai in WA. More information in the official [guide](https://gethelp.wildapricot.com/en/articles/484) that describes the steps.

### Compile & deploy
In order to compile the project you just need to execute the following maven command:
```
mvn clean install sakai:deploy
```
### Logging
The component has detailed logs to see what's happening in the background, it's really usefull to enable them in the **sakai.properties** file.
```
log.config.count = 3
log.config.1 = INFO.com.longsight.wa.jobs.WAContactsSyncJob
log.config.2 = INFO.com.longsight.wa.jobs.WAMemberGroupsSyncJob
log.config.3 = INFO.com.longsight.wa.jobs.WACourseMembershipSyncJob
```
### Courses and sites integration
Courses and sites access is granted by configuration, you can assign membership levels or member groups to courses easily. This needs to be done by **site properties**, edit the site and assign one of this properties.
* **wa-membershiplevel**. Comma separated list of membership level identifiers (Retrieved from WA)
* **wa-membergroups**. Comma separated list of members groups identifiers (Retrieved from WA)

Example:
A user which belongs to the membership level 12345 will have access to all the courses with 12345 in its **wa-membershiplevel** property.

Same for groups, a user which belongs to the group 12345 will have access to all the courses with 12345 in its **wa-membergroups** property.

You can get the Membership level identifier or the member group identifier from WA urls, **they are integers like mLevelId=971975 or mGroupId=447081**.

### Jobs
The integration and the three use cases basically rely on synchronization jobs, which are executed periodically. Create three jobs in the Sakai instance using the **Job Scheduler** tool and assign them some triggers, one valid configuration could be execute them every 15 minutes but this configuration is totally flexible.

Example of the trigger:
```
0 0/15 * * * ?
```


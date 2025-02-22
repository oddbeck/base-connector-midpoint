package no.yyz.hibernateutil.services;


import no.yyz.models.models.UserGroups;

public class UserGroupsService extends StorageService<UserGroups> implements AutoCloseable {
    public UserGroupsService() {
        super(UserGroups.class);
    }
}

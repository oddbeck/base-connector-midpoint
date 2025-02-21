package no.yyz.services;

import no.yyz.models.UserGroups;

public class UserGroupsService extends StorageService<UserGroups> implements AutoCloseable {
    public UserGroupsService() {
        super(UserGroups.class);
    }
}

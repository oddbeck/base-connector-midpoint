package no.yyz.services;

import no.yyz.models.User;

public class UserService extends StorageService<User> {
    public UserService() {
        super(User.class);
    }

}

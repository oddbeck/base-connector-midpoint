package no.yyz.hibernateutil.services;


import no.yyz.models.models.User;

public class UserService extends StorageService<User> {
    public UserService() {
        super(User.class);
    }

}

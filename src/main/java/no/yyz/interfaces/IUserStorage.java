package no.yyz.interfaces;

import no.yyz.models.Group;
import no.yyz.models.User;

import java.sql.SQLException;
import java.util.List;

public interface IUserStorage {

    User fetchUserById(int id) throws SQLException;
    List<User> fetchAllUsers() throws SQLException;

    Group fetchGroupById(int id);

    Group insertGroup(Group group) throws SQLException;

    boolean deleteGroupById(int id) throws SQLException;

    User insertUser(User user) throws SQLException;

    boolean deleteUserById(int id) throws SQLException;

    // Function to delete a user from a specific group
    boolean deleteUserFromGroup(String url, int userId, int groupId);
}

package no.yyz.services;

import no.yyz.interfaces.IUserStorage;
import no.yyz.models.Group;
import no.yyz.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IUserStorageImpl implements IUserStorage {

    private final SqlLiteStorage sqlStorage;

    public IUserStorageImpl(String storageName) {
        sqlStorage = new SqlLiteStorage(storageName);
        createUsersTable();
        createGroupsTable();
        createUsersGroupsTable();
    }

    public void createUsersGroupsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS UsersGroups ("
                + "user_id INTEGER NOT NULL, "
                + "group_id INTEGER NOT NULL, "
                + "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "PRIMARY KEY (user_id, group_id), "
                + "FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE, "
                + "FOREIGN KEY (group_id) REFERENCES Groups (id) ON DELETE CASCADE);";
        try {
            executeTableCreation(createTableSQL, "Users");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void createUsersTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "email TEXT NOT NULL);";

        try {
            executeTableCreation(createTableSQL, "Users");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Function to create Groups table
    void createGroupsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Groups ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "group_name TEXT NOT NULL, "
                + "description TEXT);";

        try {
            executeTableCreation(createTableSQL, "Groups");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeTableCreation(String createTableSQL, String tableName) throws SQLException {
        try (Statement stmt = sqlStorage.connect().createStatement()) {
            // Execute the table creation SQL
            stmt.executeUpdate(createTableSQL);
            System.out.println(tableName + " table is ready.");
        } catch (SQLException e) {
            System.out.println("Error creating " + tableName + " table: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public User insertUser(User user) throws SQLException {
        String insertSQL = "INSERT INTO Users (username, email) VALUES (?, ?)";
        int generatedId = 0;

        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(insertSQL)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());

            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1); // Retrieve the first generated key
                }
            }
            System.out.println("User inserted successfully.");
        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
            throw e;
        }
        user.setId(generatedId);
        return user;
    }

    @Override
    public Group insertGroup(Group group) throws SQLException {
        String insertSQL = "INSERT INTO Groups (group_name, description) VALUES (?, ?)";

        int generatedId = 0;
        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(insertSQL)) {

            pstmt.setString(1, group.getGroupName());
            pstmt.setString(2, group.getDescription());

            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1); // Retrieve the first generated key
                }
            }
            System.out.println("Group inserted successfully.");
        } catch (SQLException e) {
            System.out.println("Error inserting group: " + e.getMessage());
            throw e;
        }
        group.setId(generatedId);
        return group;
    }

    // Function to delete a User by ID
    @Override
    public boolean deleteUserById(int userId) throws SQLException {
        String deleteSQL = "DELETE FROM Users WHERE id = ?";
        boolean success = false;

        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(deleteSQL)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User with ID " + userId + " deleted successfully.");
                success = true;
            } else {
                System.out.println("User with ID " + userId + " not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            throw e;
        }
        return success;
    }

    // Function to delete a Group by ID
    @Override
    public boolean deleteGroupById(int groupId) throws SQLException {
        String deleteSQL = "DELETE FROM Groups WHERE id = ?";
        boolean success = false;

        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(deleteSQL)) {

            pstmt.setInt(1, groupId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Group with ID " + groupId + " deleted successfully.");
                success = true;
            } else {
                System.out.println("Group with ID " + groupId + " not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting group: " + e.getMessage());
            throw e;
        }
        return success;
    }

    // Function to fetch a User by ID
    @Override
    public User fetchUserById(int userId) throws SQLException {
        String selectSQL = "SELECT id, username, email FROM Users WHERE id = ?";
        User user = null;

        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(selectSQL)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(rs.getString("username"), rs.getString("email"));
                user.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user: " + e.getMessage());
            throw e;
        }
        return user;
    }

    @Override
    public List<User> fetchAllUsers() throws SQLException {
        String selectSQL = "SELECT * FROM Users";
        ArrayList<User> allFoundUsers = new ArrayList<>();

        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(selectSQL)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = null;
                user = new User(rs.getString("username"), rs.getString("email"));
                user.setId(rs.getInt("id"));
                allFoundUsers.add(user);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user: " + e.getMessage());
            throw e;
        }
        return allFoundUsers;
    }

    @Override
    public Group fetchGroupById(int groupId) {
        String selectSQL = "SELECT id, group_name, description FROM Groups WHERE id = ?";
        Group group = null;

        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(selectSQL)) {

            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                group = new Group(rs.getString("group_name"), rs.getString("description"));
                group.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching group: " + e.getMessage());
        }

        return group;
    }

    public void insertUserIntoGroup(String url, int userId, int groupId) {
        String insertSQL = "INSERT INTO UsersGroups (user_id, group_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(insertSQL)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);

            pstmt.executeUpdate();
            System.out.println("User " + userId + " added to group " + groupId + " successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding user to group: " + e.getMessage());
        }
    }


    // Function to fetch all users in a specific group
    public void fetchUsersInGroup(String url, int groupId) {
        String selectSQL = "SELECT u.id, u.username, u.email FROM Users u "
                + "JOIN UsersGroups ug ON u.id = ug.user_id "
                + "WHERE ug.group_id = ?";

        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(selectSQL)) {

            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("User ID: " + rs.getInt("id")
                        + ", Username: " + rs.getString("username")
                        + ", Email: " + rs.getString("email"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
    }

    // Function to delete a user from a specific group
    @Override
    public boolean deleteUserFromGroup(String url, int userId, int groupId) {
        String deleteSQL = "DELETE FROM UsersGroups WHERE user_id = ? AND group_id = ?";

        int rowsAffected = 0;
        try (PreparedStatement pstmt = sqlStorage.connect().prepareStatement(deleteSQL)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);

            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User " + userId + " removed from group " + groupId + " successfully.");
            } else {
                System.out.println("No association found between user " + userId + " and group " + groupId);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting user from group: " + e.getMessage());
        }
        return rowsAffected > 0;
    }

}

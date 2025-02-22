package no.yyz.models.models;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
@Table(name = "Groups")
public class Group extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    private String groupName;
    private String description;
    private ArrayList<User> members;

    // Constructor
    public Group(String groupName, String description){
        this.groupName = groupName;
        this.description = description;
    }
    public Group() {

    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<User> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<User> members) {
        this.members = members;
    }
}

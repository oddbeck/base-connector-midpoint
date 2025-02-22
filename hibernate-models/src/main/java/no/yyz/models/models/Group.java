package no.yyz.models.models;

import jakarta.persistence.*;
import org.identityconnectors.framework.common.objects.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Groups")
public class Group extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    private String groupName;
    private String description;
    private ArrayList<Integer> members;

    // Constructor
    public Group(String groupName, String description) {
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

    public ArrayList<Integer> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Integer> members) {
        this.members = members;
    }

    public static ObjectClassInfoBuilder ObjectInfoBuilder() {

        ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
        objectClassBuilder.setType(ObjectClass.GROUP_NAME);
        AttributeInfo groupName = new AttributeInfoBuilder("groupName", String.class).setRequired(true).build();
        objectClassBuilder.addAttributeInfo(groupName);
        objectClassBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("description", String.class));
        var members = new AttributeInfoBuilder("members", String.class);
        members.setMultiValued(true);
        objectClassBuilder.addAttributeInfo(members.build());
        return objectClassBuilder;
    }

    public void parseAttributes(Set<Attribute> set) {
        for (Attribute attribute : set) {
            String name = attribute.getName();
            List<Object> value = attribute.getValue();
            Object firstValue = null;
            if (!value.isEmpty()) {
                firstValue = value.getFirst();
            }
            switch (name.toLowerCase()) {
                case "description": {
                    if (firstValue != null) {
                        setDescription(firstValue.toString());
                    }
                    break;
                }
                case "groupname": {
                    if (firstValue != null) {
                        setGroupName(firstValue.toString());
                    }
                    break;
                }
                case "members": {
                    members = new ArrayList<>();
                    for (var v : value) {
                        members.add(Integer.parseInt(v.toString()));
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }
}

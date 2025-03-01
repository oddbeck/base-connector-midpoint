package no.yyz.models.models;

import jakarta.persistence.*;
import org.identityconnectors.framework.common.objects.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    private String username;
    private String email;
    private String givenName;
    private String lastName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    private String fullName;

    // Constructor
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public User() {
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static ObjectClassInfoBuilder ObjectInfoBuilder() {

        ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
        objectClassBuilder.setType(ObjectClass.ACCOUNT_NAME);

        AttributeInfo username = new AttributeInfoBuilder("username", String.class).setRequired(true).build();

        objectClassBuilder.addAttributeInfo(username);

        objectClassBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("email", String.class));

        objectClassBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("givenName", String.class));

        objectClassBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("lastName", String.class));

        objectClassBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("fullName", String.class));

        return objectClassBuilder;
    }

    public Set<Attribute> toAttributes() {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(AttributeBuilder.build("username", this.getUsername()));
        attributes.add(AttributeBuilder.build("email", this.getEmail()));
        attributes.add(AttributeBuilder.build("givenName", this.getGivenName()));
        attributes.add(AttributeBuilder.build("lastName", this.getLastName()));
        attributes.add(AttributeBuilder.build("fullName", this.getFullName()));
        attributes.add(AttributeBuilder.build(Uid.NAME, Integer.toString(getId())));
        attributes.add(AttributeBuilder.build(Name.NAME, getUsername()));
        return attributes;
    }

    public void parseAttributesDelta(Set<AttributeDelta> attributes) {
        for (AttributeDelta attribute : attributes) {
            String name = attribute.getName();
            List<Object> value = attribute.getValuesToReplace();
            Object firstValue = null;
            if (!value.isEmpty()) {
                firstValue = value.getFirst();
            }
            switch (name.toLowerCase()) {
                case "email": {
                    setEmail((String) firstValue);
                    break;
                }
                case "username": {
                    setUsername((String) firstValue);
                    break;
                }
                case "givenname": {
                    setGivenName((String) firstValue);
                    break;
                }
                case "lastname": {
                    setLastName((String) firstValue);
                    break;
                }
                case "__name__", "fullname": {
                    setFullName((String) firstValue);
                    break;
                }
                default:
                    break;
            }
        }
    }
    public void parseAttributes(Set<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            String name = attribute.getName();
            List<Object> value = attribute.getValue();
            Object firstValue = null;
            if (!value.isEmpty()) {
                firstValue = value.getFirst();
            }
            switch (name.toLowerCase()) {
                case "email": {
                    setEmail((String) firstValue);
                    break;
                }
                case "username": {
                    setUsername((String) firstValue);
                    break;
                }
                case "givenname": {
                    setGivenName((String) firstValue);
                    break;
                }
                case "lastname": {
                    setLastName((String) firstValue);
                    break;
                }
                case "__name__", "fullname": {
                    setFullName((String) firstValue);
                    break;
                }
                default:
                    break;
            }
        }
    }
}


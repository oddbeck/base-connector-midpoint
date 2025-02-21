package no.yyz.models;

import jakarta.persistence.*;
import org.identityconnectors.framework.common.objects.*;

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

//        AttributeInfoBuilder uidAib = new AttributeInfoBuilder(Uid.NAME);
//        uidAib.setNativeName("id");
//        uidAib.setType(Integer.class);
//        uidAib.setRequired(false); // Must be optional. It is not present for create operations
//        uidAib.setCreateable(false);
//        uidAib.setUpdateable(false);
//        uidAib.setReadable(true);
//        objectClassBuilder.addAttributeInfo(uidAib.build());
//
//        AttributeInfoBuilder nameAib = new AttributeInfoBuilder(Name.NAME);
//        nameAib.setType(String.class);
//        nameAib.setNativeName("username");
//        nameAib.setRequired(true);
//        objectClassBuilder.addAttributeInfo(nameAib.build());
        return objectClassBuilder;
    }
}


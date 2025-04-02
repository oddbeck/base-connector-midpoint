package no.yyz.models.models;

import jakarta.persistence.*;
import org.identityconnectors.framework.common.objects.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User { // extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  private String email;
  private String givenName;
  private String lastName;
  private String fullName;
  @ManyToMany
  @JoinTable(name = "users_groups", joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private Set<Group> groups = new HashSet<>();

  // Constructor
  public User(String username, String email) {
    this.username = username;
    this.email = email;
  }

  public User() {
  }

  public static ObjectClassInfoBuilder ObjectInfoBuilder() {

    ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
    objectClassBuilder.setType(ObjectClass.ACCOUNT_NAME);

    AttributeInfo username =
        new AttributeInfoBuilder("username", String.class).setRequired(true).build();

    objectClassBuilder.addAttributeInfo(username);

    objectClassBuilder.addAttributeInfo(
        AttributeInfoBuilder.build("email", String.class));

    objectClassBuilder.addAttributeInfo(
        AttributeInfoBuilder.build("givenName", String.class));

    objectClassBuilder.addAttributeInfo(
        AttributeInfoBuilder.build("lastName", String.class));

    objectClassBuilder.addAttributeInfo(
        AttributeInfoBuilder.build("fullName", String.class));

    AttributeInfoBuilder members = new AttributeInfoBuilder("memberOf", String.class)
        .setMultiValued(true);
    objectClassBuilder.addAttributeInfo(members.build());

    return objectClassBuilder;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Set<Group> getGroups() {
    return groups;
  }

  public void setGroups(Set<Group> groups) {
    this.groups = groups;
  }

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

  // Getters and setters

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

  public Set<Attribute> toAttributes() {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(AttributeBuilder.build("username", this.getUsername()));
    attributes.add(AttributeBuilder.build("email", this.getEmail()));
    attributes.add(AttributeBuilder.build("givenName", this.getGivenName()));
    attributes.add(AttributeBuilder.build("lastName", this.getLastName()));
    attributes.add(AttributeBuilder.build("fullName", this.getFullName()));

    List<String> memberOf = this.getGroups().stream().map(Group::getId).map(String::valueOf).toList();
    attributes.add(AttributeBuilder.build("memberOf", memberOf));

    attributes.add(AttributeBuilder.build(Uid.NAME, Long.toString(getId())));
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


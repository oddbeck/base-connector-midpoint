package no.yyz.models.models;

import jakarta.persistence.*;
import org.identityconnectors.framework.common.objects.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "groups")
public class Group { // extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String groupName;
  private String description;

  @ManyToMany(mappedBy = "groups")
  private Set<User> users = new HashSet<>();

  // Constructor
  public Group(String groupName, String description) {
    this.groupName = groupName;
    this.description = description;
  }

  public Group() {
  }

  public static ObjectClassInfoBuilder ObjectInfoBuilder() {

    ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
    objectClassBuilder.setType(ObjectClass.GROUP_NAME);
    AttributeInfo groupName =
        new AttributeInfoBuilder("groupName", String.class).setRequired(true).build();
    objectClassBuilder.addAttributeInfo(groupName);
    objectClassBuilder.addAttributeInfo(
        AttributeInfoBuilder.build("description", String.class));
    AttributeInfoBuilder members = new AttributeInfoBuilder("members", String.class)
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

  //  @ManyToMany(mappedBy = "groups")
  public Set<User> getUsers() {
    return users;
  }

  public void setUsers(Set<User> users) {
    this.users = users;
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

  public Set<Attribute> toAttributes() {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(AttributeBuilder.build(Uid.NAME, Long.toString(id)));
    attributes.add(AttributeBuilder.build(Name.NAME, groupName));
    attributes.add(AttributeBuilder.build("description", description));
    attributes.add(AttributeBuilder.build("groupName", groupName));
    // convert the 'users' getUsername() contents to an arraylist
    List<String> members = users.stream().map(User::getId).map(String::valueOf).collect(Collectors.toList());

    attributes.add(AttributeBuilder.build("members", members));
    return attributes;
  }

  /**
   * Applies a set of attribute deltas to this object, updating the respective
   * fields.
   *
   * @param set a set of attribute deltas to apply
   */
  public void parseAttributesDelta(Set<AttributeDelta> set) {
    for (AttributeDelta attribute : set) {
      String name = attribute.getName();
      List<Object> value = attribute.getValuesToReplace();
      Object firstValue = null;
      if (!value.isEmpty()) {
        firstValue = value.getFirst();
      }
      switch (name.toLowerCase()) {
        case "__name__", "groupname": {
          setGroupName((String) firstValue);
          break;
        }
        case "description": {
          setDescription((String) firstValue);
          break;
        }
//        case "members": {
//          members = new ArrayList<>();
//          for (var v : value) {
//            members.add(Long.parseLong(v.toString()));
//          }
//          break;
//        }
        default:
          break;
      }
    }
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
        case "__name__", "groupname": {
          setGroupName((String) firstValue);
          break;
        }
        case "description": {
          setDescription((String) firstValue);
          break;
        }
//        case "members": {
//          members = new ArrayList<>();
//          for (var v : value) {
//            members.add(Long.parseLong(v.toString()));
//          }
//          break;
//        }
        default:
          break;
      }
    }
  }
}

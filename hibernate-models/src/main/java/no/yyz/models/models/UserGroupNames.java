package no.yyz.models.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(UserGroupsNamesCompoundKey.class)
@Table(name = "user_group_names")

public class UserGroupNames extends BaseModel {
  @Id
  private String userName;
  @Id
  private String groupName;

  public UserGroupNames(String userName, String groupName) {
    this.userName = userName;
    this.groupName = groupName;
  }
  public UserGroupNames() {

  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }
}


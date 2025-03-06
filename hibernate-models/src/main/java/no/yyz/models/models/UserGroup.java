package no.yyz.models.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(UserGroupsCompoundKey.class)
@Table(name = "UsersGroups")
public class UserGroup extends BaseModel {

    @Id
    public int userId;
    @Id
    public int groupId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public UserGroup() {
    }

    public UserGroup(int userId, int groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }
}


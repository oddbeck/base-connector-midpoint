package no.yyz.models;

import java.io.Serializable;

public record UserGroupsCompoundKey (int userId, int groupId) implements Serializable {
}

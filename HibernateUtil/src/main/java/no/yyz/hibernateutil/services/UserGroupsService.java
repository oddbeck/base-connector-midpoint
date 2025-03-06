package no.yyz.hibernateutil.services;


import no.yyz.models.models.UserGroup;
import org.hibernate.Session;

import java.util.List;

public class UserGroupsService extends StorageService<UserGroup> implements AutoCloseable {
    public UserGroupsService() {
        super(UserGroup.class);
    }

    public List<Integer> getGroupMembersByGroupId(int groupId, Session session) {
        boolean createdSession = false;
        try {
            if (session == null) {
                createdSession = true;
                session = sessionFactory.openSession();
            }
            return session.createQuery("Select userId from UsersGroups where groupId = :groupId",
                Integer.class).setParameter("groupId", groupId).getResultList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null && createdSession) {
                session.close();
            }
        }
    }
    public List<Integer> getGroupsByUserId(int userId, Session session) {
        boolean createdSession = false;
        try {
            if (session == null) {
                createdSession = true;
                session = sessionFactory.openSession();
            }
            return session.createQuery("Select groupId from UsersGroups where userId = :userId",
                Integer.class).setParameter("userId", userId).getResultList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null && createdSession) {
                session.close();
            }
        }
    }
}

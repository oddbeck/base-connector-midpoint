package no.yyz.hibernateutil.services;


import no.yyz.models.models.UserGroups;
import org.hibernate.Session;

import java.util.List;

public class UserGroupsService extends StorageService<UserGroups> implements AutoCloseable {
    public UserGroupsService() {
        super(UserGroups.class);
    }

    public List<Integer> getGroupMembersByGroupId(int groupId, Session session) {
        boolean createdSession = false;
        try {
            if (session == null) {
                createdSession = true;
                session = sessionFactory.openSession();
            }
            return session.createQuery("Select userId from UserGroups where groupId = :groupId", Integer.class).setParameter("groupId", groupId).getResultList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null && createdSession) {
                session.close();
            }
        }
    }
}

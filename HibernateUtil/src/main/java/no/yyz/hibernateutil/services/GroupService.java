package no.yyz.hibernateutil.services;


import no.yyz.models.models.Group;
import org.hibernate.Session;

public class GroupService extends StorageService<Group> {

    public GroupService() {
        super(Group.class);
    }

    public Group findGroupByName(String name, Session session) {

        boolean createdSession = false;
        if (session == null) {
            session = this.sessionFactory.openSession();
            createdSession = true;
        }
        try {
            var cb = this.sessionFactory.getCriteriaBuilder();
            var cq = cb.createQuery(Group.class);
            // create query to find group by name
            cq.where(cb.equal(cq.from(Group.class).get("groupName"), name));
            // query for it
            var query = session.createQuery(cq);
            return query.stream().findFirst().orElse(null);

        } catch (Exception e) {
            if (createdSession) {
                session.close();
            }
            throw new RuntimeException(e);
        }
        finally
        {
            if (createdSession) {
                session.close();
            }
        }
    }
    public Group findGroupByName(String name) {
        try (var session = this.sessionFactory.openSession()) {
            return findGroupByName(name, session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

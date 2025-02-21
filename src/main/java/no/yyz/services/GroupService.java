package no.yyz.services;

import no.yyz.models.Group;

public class GroupService extends StorageService<Group> {

    public GroupService() {
        super(Group.class);
    }

    public Group findGroupByName(String name) {

        try (var session = this.sessionFactory.openSession()) {
            var cb = this.sessionFactory.getCriteriaBuilder();
            var cq = cb.createQuery(Group.class);
           // create query to find group by name
            cq.where(cb.equal(cq.from(Group.class).get("groupName"), name));
            // query for it
            var query = session.createQuery(cq);
            return query.stream().findFirst().orElse(null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

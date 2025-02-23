package no.yyz.hibernateutil.services;


import no.yyz.models.models.User;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;

public class UserService extends StorageService<User> {
    public UserService() {
        super(User.class);
    }

    public User findByName(String name, Session session) {
        boolean sessionCreated = false;
        if (session == null) {
            session = this.sessionFactory.openSession();
            sessionCreated = true;
        }
        try {
            HibernateCriteriaBuilder cb = this.sessionFactory.getCriteriaBuilder();
            JpaCriteriaQuery<User> cq = cb.createQuery(User.class);
            cq.where(cb.equal(cq.from(User.class).get("username"), name));
            Query<User> query = session.createQuery(cq);
            return query.stream().findFirst().orElse(null);
        } catch (Exception e) {
            session.close();
            throw new RuntimeException(e);
        } finally {
            if (session != null && sessionCreated) {
                session.close();
            }
        }
    }

    public User findByName(String name) {
        try (Session session = this.sessionFactory.openSession()) {
            return findByName(name, session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

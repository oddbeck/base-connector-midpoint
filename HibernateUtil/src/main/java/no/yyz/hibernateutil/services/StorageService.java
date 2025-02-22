package no.yyz.hibernateutil.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import no.yyz.models.models.BaseModel;
import org.hibernate.Session;

import java.util.List;

public class StorageService<T extends BaseModel> extends AbstractService implements AutoCloseable {

    Class<T> type;

    protected StorageService(Class<T> type) {
        super();
        this.type = type;
    }

    public T persist(T t, Session session) {
        boolean createdSession = false;
        try {
            if (session == null) {
                session = sessionFactory.openSession();
                createdSession = true;
            }
            var transaction = session.getTransaction();
            transaction.begin();
            session.persist(t);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (session != null && createdSession) {
                session.close();
            }
        }
        return t;
    }

    public T persist(T t) {
        try (var session = sessionFactory.openSession()) {
            persist(t, session);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return t;
    }

    public T getById(int id) {
        boolean createdSession = false;
        try {
            Session session = sessionFactory.openSession();
            return getById(id, session);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public T getById(int id, Session session) {
        boolean createdSession = false;
        try {
            if (session == null) {
                session = sessionFactory.openSession();
                createdSession = true;
            }
            var transaction = session.beginTransaction();
            T result = session.get(this.type, id);
            transaction.commit();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (session != null && createdSession) {
                session.close();
            }
        }
    }

    public void delete(T t) {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.remove(t);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public T update(T t, int id) {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.getTransaction();
            transaction.begin();
            var foundItem = session.find(t.getClass(), id);
            if (foundItem == null) return null;
            session.merge(t);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return t;
    }

    public List<T> getAll() {
        try (var session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaQuery<T> criteria = cb.createQuery(this.type);
            criteria.from(this.type);
            return session.createQuery(criteria).list();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void close() throws Exception {
        sessionFactory.close();
    }
}

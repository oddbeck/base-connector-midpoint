package no.yyz.services;

import no.yyz.hibernate.HibernateUtil;
import org.hibernate.SessionFactory;

public abstract class AbstractService {

    protected final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
}

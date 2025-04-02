package no.yyz.hibernateutil.services;

import no.yyz.hibernateutil.HibernateUtil;
import org.hibernate.SessionFactory;

public class SessionFactoryService {

    public final SessionFactory sessionFactory;

    public SessionFactoryService() {
        sessionFactory = HibernateUtil.createSessionFactory(
                "jdbc:sqlite:test.sqlite",
                "org.sqlite.JDBC",
                "org.hibernate.community.dialect.SQLiteDialect",
                "username",
                "password"
        );

    }

    SessionFactoryService(String jdbcUrl, String driverClassName, String hibernateDialect, String username, String password, String name) {
        sessionFactory = HibernateUtil.createSessionFactory(jdbcUrl, driverClassName, hibernateDialect, username, password);
    }
}

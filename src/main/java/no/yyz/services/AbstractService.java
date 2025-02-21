package no.yyz.services;

import no.yyz.hibernate.HibernateUtil;
import org.hibernate.SessionFactory;

public abstract class AbstractService {

    protected final SessionFactory sessionFactory;

    AbstractService() {
        sessionFactory = HibernateUtil.createSessionFactory(
                "jdbc:sqlite:test.sqlite",
                "org.sqlite.JDBC",
                "org.hibernate.community.dialect.SQLiteDialect",
                "username",
                "password"
        );

    }

    AbstractService(String jdbcUrl, String driverClassName, String hibernateDialect, String username, String password, String name) {
        sessionFactory = HibernateUtil.createSessionFactory(jdbcUrl, driverClassName, hibernateDialect, username, password);
    }
}

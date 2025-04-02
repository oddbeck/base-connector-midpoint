package no.yyz.hibernateutil;

import no.yyz.models.models.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {

  static SessionFactory sessionFactory;

  public static SessionFactory createSessionFactory(String jdbcUrl, String driverClassName,
                                                    String hibernateDialect, String username,
                                                    String password) {
    Configuration configuration = new Configuration()
        .addAnnotatedClass(User.class)
        .addAnnotatedClass(Group.class)
        .setProperty("hibernate.connection.driver_class", driverClassName)
        .setProperty("hibernate.connection.url", jdbcUrl)
        .setProperty("hibernate.dialect", hibernateDialect)
        .setProperty("hibernate.show_sql", "true")
        .setProperty("hibernate.connection.username", username)
        .setProperty("hibernate.connection.password", password)
        .setProperty("hibernate.hbm2ddl.auto", "update");  // Corrected typo here
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .applySettings(configuration.getProperties()).build();
    return configuration.buildSessionFactory(serviceRegistry);

  }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static SessionFactory getSessionFactory(String name) {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}

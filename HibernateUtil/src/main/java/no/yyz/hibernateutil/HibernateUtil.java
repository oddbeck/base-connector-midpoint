package no.yyz.hibernateutil;

import no.yyz.models.models.Group;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import no.yyz.models.models.User;
import no.yyz.models.models.UserGroup;

public class HibernateUtil {

    public static SessionFactory createSessionFactory(String jdbcUrl, String driverClassName, String hibernateDialect, String username, String password) {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Group.class)
                .addAnnotatedClass(UserGroup.class)
                .setProperty("hibernate.connection.driver_class", driverClassName)
                //.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC")
                .setProperty("hibernate.connection.url", jdbcUrl)
                //.setProperty("hibernate.connection.url", "jdbc:sqlite:test.sqlite")
                .setProperty("hibernate.dialect", hibernateDialect)
                // .setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect")
                //.setProperty("hibernate.show_sql", "true")
                // set hibernate username
                .setProperty("hibernate.connection.username", username)
                .setProperty("hibernate.connection.password", password)
                .setProperty("hibernate.hbm2ddl.auto", "update");  // Corrected typo here
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();
        return configuration.buildSessionFactory(serviceRegistry);

    }

//    public static SessionFactory getSessionFactory() {
//        return sessionFactory;
//    }
//
//    public static SessionFactory getSessionFactory(String name) {
//        return sessionFactory;
//    }
//
//    public static void shutdown() {
//        if (sessionFactory != null) {
//            sessionFactory.close();
//        }
//    }
}

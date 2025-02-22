package no.yyz;

import no.yyz.hibernateutil.HibernateUtil;
import org.hibernate.SessionFactory;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationClass;
import org.identityconnectors.framework.spi.ConfigurationProperty;

@ConfigurationClass(
        skipUnsupported = true
)
public class YyzConfiguration extends AbstractConfiguration {

    private String host;
    SessionFactory sessionFactory;

    @Override
    public void validate() {
        sessionFactory = HibernateUtil.createSessionFactory("jdbc:sqlite:test.sqlite", "org.sqlite.JDBC", "org.hibernate.community.dialect.SQLiteDialect", "username", "password");
    }

    @ConfigurationProperty(required = false,
            helpMessageKey = "UI_CONNECTOR_HOST_HELP",
            displayMessageKey = "UI_CONNECTOR_HOST_DISPLAY")
    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}

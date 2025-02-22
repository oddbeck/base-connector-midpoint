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

    @Override
    public void validate() {
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

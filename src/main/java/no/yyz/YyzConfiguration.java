package no.yyz;

import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationClass;
import org.identityconnectors.framework.spi.ConfigurationProperty;

@ConfigurationClass(
        skipUnsupported = true
)
public class YyzConfiguration extends AbstractConfiguration {

    private String fakeName;

    @Override
    public void validate() {

    }

    private String instanceId;

    @ConfigurationProperty(displayMessageKey = "UI_INSTANCE_ID",
            helpMessageKey = "UI_INSTANCE_ID_HELP")
    public String getInstanceId() {
        return instanceId;
    }

    @ConfigurationProperty(displayMessageKey = "UI_FAKE_NAME",
            helpMessageKey = "UI_FAKE_NAME_HELP")
    public String getFakeName() {
        return fakeName;
    }

    public void setFakeName(String config) {
        this.fakeName = config;
    }

    public void setInstanceId(String config) {
        this.instanceId = config;
    }
}

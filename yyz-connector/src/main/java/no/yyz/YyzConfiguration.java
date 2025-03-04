package no.yyz;

import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationClass;
import org.identityconnectors.framework.spi.ConfigurationProperty;

@ConfigurationClass(
    skipUnsupported = true
)
public class YyzConfiguration extends AbstractConfiguration {

  private String jdbcUrl;
  private String host;
  @Override
  public void validate() {
  }

  @ConfigurationProperty(
      required = false ,
      displayMessageKey = "jdbcUrl",
      helpMessageKey = "jdbcUrl"
  )
  public String getJdbcUrl() {
    return jdbcUrl;

  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  @ConfigurationProperty(
      required = false ,
      displayMessageKey = "get host",
      helpMessageKey = "get host"
  )
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }
}

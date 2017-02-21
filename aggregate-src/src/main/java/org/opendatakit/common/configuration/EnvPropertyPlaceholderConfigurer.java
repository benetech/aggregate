package org.opendatakit.common.configuration;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class EnvPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

  private static final Log logger = LogFactory
      .getLog(EnvPropertyPlaceholderConfigurer.class.getName());

  @Override
  protected String resolvePlaceholder(String placeholder, Properties props) {
    String envVar = placeholder.replace(".", "_").toUpperCase();
    String result = System.getenv(envVar);
    if (envVar.contains("JDBC_URL")) {
      logger.info("Spot-checking " + placeholder + " as " + envVar + " and finding " + result);
    }
    return StringUtils.isNotEmpty(result) ? result : super.resolvePlaceholder(placeholder, props);
  }
}

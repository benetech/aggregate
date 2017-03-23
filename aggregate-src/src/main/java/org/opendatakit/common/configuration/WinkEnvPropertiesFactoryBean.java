package org.opendatakit.common.configuration;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;

/**
 * 
 * Here we're bending over backwards to
 *
 */
public class WinkEnvPropertiesFactoryBean extends PropertiesFactoryBean {

  private static final Log logger = LogFactory.getLog(WinkEnvPropertiesFactoryBean.class.getName());

  public WinkEnvPropertiesFactoryBean() {

  }

  @Override
  protected Properties createProperties() throws IOException {
    Properties properties = super.createProperties();

    logger.error("Creating Properties for Wink");
    String result = System.getenv("EXTERNAL_ROOT_URL");
    logger.error("EXTERNAL_ROOT_URL is " + result);
    if (StringUtils.isNotEmpty(result)) {
      properties.setProperty("wink.http.uri", result);
      properties.setProperty("wink.https.uri", result);
    }
    properties.setProperty("wink.handlersFactoryClass", "org.opendatakit.aggregate.odktables.impl.api.wink.AppEngineHandlersFactory");
    return properties;
  }

}

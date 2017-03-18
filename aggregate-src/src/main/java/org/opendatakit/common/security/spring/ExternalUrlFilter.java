/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.security.spring;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * Wraps the Spring class and ensures that if an Authentication is already
 * determined for this request, that it isn't overridden.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class ExternalUrlFilter implements Filter,  
    EnvironmentAware {

  Environment environment;


  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    Log log = LogFactory.getLog(this.getClass());

    String externalUrlString = environment.getProperty("external.root.url");
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String requestURIString = httpRequest.getRequestURI();
    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(httpRequest);

    try {
      URI externalURI = new URI(externalUrlString);

      URI requestURI;

      requestURI = new URI(requestURIString);

      log.error("request url " + requestURIString);
      log.error("external.root.url " + externalUrlString);

      if (StringUtils.isNotBlank(externalUrlString)
          && !equivalentBaseURI(requestURI, externalURI)) {
        log.error("they're not equal.");
        request.getRequestDispatcher(requestURIString).forward(request, response);

      }
    } catch (URISyntaxException e) {
      log.error(e.getStackTrace());
    }

    chain.doFilter(request, response);

  }

  private String targetURI(URI targetBase, URI targetPath) throws URISyntaxException {
    URI result = new URI(targetBase.getScheme(), targetPath.getUserInfo(), targetBase.getHost(),
        targetBase.getPort(), targetPath.getPath(), targetPath.getQuery(),
        targetPath.getFragment());
    return result.toString();

  }

  private boolean equivalentBaseURI(URI uri1, URI uri2) {
    uri1 = uri1.normalize();
    uri2 = uri2.normalize();
    return uri1.getScheme() != null && uri2.getScheme() != null && uri1.getHost() != null
        && uri2.getHost() != null && uri1.getPort() != -1 && uri2.getPort() != -1
        && uri1.getScheme().equals(uri2.getScheme()) && uri1.getHost().equals(uri2.getHost())
        && uri1.getPort() == uri2.getPort();
  }



  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }


  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
   
  }



}

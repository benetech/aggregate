/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.task.testing.gae;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.task.CsvGenerator;
import org.opendatakit.aggregate.task.FormDelete;
import org.opendatakit.aggregate.task.JsonFileGenerator;
import org.opendatakit.aggregate.task.KmlGenerator;
import org.opendatakit.aggregate.task.PurgeOlderSubmissions;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.aggregate.task.Watchdog;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.aggregate.util.ImageUtil;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.springframework.beans.factory.InitializingBean;

public class WatchdogImpl implements Watchdog, InitializingBean {
  /** cached value of the fast-publishing flag */
  private boolean lastFastPublishingEnabledFlag = false;

  Datastore datastore = null;
  UserService userService = null;
  UploadSubmissions uploadSubmissions = null;
  CsvGenerator csvGenerator = null;
  KmlGenerator kmlGenerator = null;
  JsonFileGenerator jsonFileGenerator = null;
  PurgeOlderSubmissions purgeSubmissions = null;
  FormDelete formDelete = null;
  WorksheetCreator worksheetCreator = null;
  ServletContext ctxt = null;
  HttpClientFactory httpClientFactory = null;
  ImageUtil imageUtil = null;

  /**
   * Implementation of CallingContext.
   *
   * @author mitchellsundt@gmail.com
   *
   */
  public class CallingContextImpl implements CallingContext {

     boolean asDaemon = true;
     String serverUrl;
     String secureServerUrl;

     CallingContextImpl() {

        Realm realm = userService.getCurrentRealm();
        Integer identifiedPort = realm.getPort();
        Integer identifiedSecurePort = realm.getSecurePort();
        String identifiedHostname = realm.getHostname();

        if ( identifiedHostname == null || identifiedHostname.length() == 0 ) {
           try {
              identifiedHostname = InetAddress.getLocalHost().getCanonicalHostName();
           } catch (UnknownHostException e) {
              identifiedHostname = "127.0.0.1";
           }
        }

        String identifiedScheme = "http";
        if ( realm.isSslRequired() ) {
           identifiedScheme = "https";
           identifiedPort = identifiedSecurePort;
        }

        if ( identifiedPort == null || identifiedPort == 0 ) {
           if ( realm.isSslRequired() ) {
              identifiedPort = HtmlConsts.SECURE_WEB_PORT;
           } else {
              identifiedPort = HtmlConsts.WEB_PORT;
           }
        }


        boolean expectedPort =
           (identifiedScheme.equalsIgnoreCase("http") &&
                 identifiedPort == HtmlConsts.WEB_PORT) ||
           (identifiedScheme.equalsIgnoreCase("https") &&
                 identifiedPort == HtmlConsts.SECURE_WEB_PORT);

        String path = "";
        if ( ctxt != null ) {
           path = ctxt.getContextPath();
        }

        if (!expectedPort) {
           serverUrl = identifiedScheme + "://" + identifiedHostname + BasicConsts.COLON +
              Integer.toString(identifiedPort) + path;
         } else {
           serverUrl = identifiedScheme + "://" + identifiedHostname + path;
         }

        if ( realm.isSslRequired() || !realm.isSslAvailable() ) {
           secureServerUrl = serverUrl;
        } else {
           if ( identifiedSecurePort != null && identifiedSecurePort != 0 &&
                 identifiedSecurePort != HtmlConsts.SECURE_WEB_PORT ) {
              // explicitly name the port
              secureServerUrl = "https://" + identifiedHostname + BasicConsts.COLON +
              Integer.toString(identifiedSecurePort) + path;
           } else {
              // assume it is the default https port...
              secureServerUrl = "https://" + identifiedHostname + path;
           }
        }
     }

     @Override
     public boolean getAsDaemon() {
        return asDaemon;
     }

     @Override
     public Object getBean(String beanName) {
        if ( BeanDefs.WATCHDOG.equals(beanName) ) {
           return WatchdogImpl.this;
        } else if ( BeanDefs.CSV_BEAN.equals(beanName) ) {
           return csvGenerator;
        } else if ( BeanDefs.DATASTORE_BEAN.equals(beanName)) {
           return datastore;
        } else if ( BeanDefs.FORM_DELETE_BEAN.equals(beanName)) {
           return formDelete;
        } else if ( BeanDefs.PURGE_OLDER_SUBMISSIONS_BEAN.equals(beanName)) {
           return purgeSubmissions;
        } else if ( BeanDefs.KML_BEAN.equals(beanName)) {
           return kmlGenerator;
        } else if ( BeanDefs.JSON_FILE_BEAN.equals(beanName)) {
            return jsonFileGenerator;
        } else if ( BeanDefs.UPLOAD_TASK_BEAN.equals(beanName)) {
           return uploadSubmissions;
        } else if ( BeanDefs.USER_BEAN.equals(beanName)) {
           return userService;
        } else if ( BeanDefs.WORKSHEET_BEAN.equals(beanName)) {
           return worksheetCreator;
        } else if ( BeanDefs.HTTP_CLIENT_FACTORY.equals(beanName)) {
           return httpClientFactory;
        } else if ( BeanDefs.IMAGE_UTIL.equals(beanName)) {
           return imageUtil;
        }
        throw new IllegalStateException("unable to locate bean");
     }

     @Override
     public User getCurrentUser() {
        return userService.getDaemonAccountUser();
     }

     @Override
     public Datastore getDatastore() {
        return datastore;
     }

     @Override
     public UserService getUserService() {
        return userService;
     }

     @Override
     public void setAsDaemon(boolean asDaemon) {
        this.asDaemon = asDaemon;
     }

     @Override
     public String getServerURL() {
        return serverUrl;
     }

     @Override
     public String getSecureServerURL() {
        return secureServerUrl;
     }

     @Override
     public ServletContext getServletContext() {
        return ctxt;
     }

     @Override
     public String getWebApplicationURL() {
       String path = "";
       if ( ctxt != null ) {
          path = ctxt.getContextPath();
       }
       return path;
     }

     @Override
     public String getWebApplicationURL(String servletAddr) {
        return getWebApplicationURL() + BasicConsts.FORWARDSLASH + servletAddr;
     }
  }

  @Override
  public void onUsage(long delayMilliseconds, CallingContext cc) {
    // NO-OP
  }

  public WatchdogImpl() {
  }

  public Datastore getDatastore() {
     return datastore;
  }

  public void setDatastore(Datastore datastore) {
     this.datastore = datastore;
  }

  public UserService getUserService() {
     return userService;
  }

  public void setUserService(UserService userService) {
     this.userService = userService;
  }

  public UploadSubmissions getUploadSubmissions() {
     return uploadSubmissions;
  }

  public void setUploadSubmissions(UploadSubmissions uploadSubmissions) {
     this.uploadSubmissions = uploadSubmissions;
  }

  public CsvGenerator getCsvGenerator() {
     return csvGenerator;
  }

  public void setCsvGenerator(CsvGenerator csvGenerator) {
     this.csvGenerator = csvGenerator;
  }

  public KmlGenerator getKmlGenerator() {
     return kmlGenerator;
  }

  public void setJsonFileGenerator(JsonFileGenerator jsonFileGenerator) {
     this.jsonFileGenerator = jsonFileGenerator;
  }

  public JsonFileGenerator getJsonFileGenerator() {
     return jsonFileGenerator;
  }

  public void setKmlGenerator(KmlGenerator kmlGenerator) {
     this.kmlGenerator = kmlGenerator;
  }

  public FormDelete getFormDelete() {
     return formDelete;
  }

  public void setFormDelete(FormDelete formDelete) {
     this.formDelete = formDelete;
  }

  public PurgeOlderSubmissions getPurgeSubmissions() {
     return purgeSubmissions;
  }

  public void setPurgeSubmissions(PurgeOlderSubmissions purgeSubmissions) {
     this.purgeSubmissions = purgeSubmissions;
  }

  public WorksheetCreator getWorksheetCreator() {
     return worksheetCreator;
  }

  public void setWorksheetCreator(WorksheetCreator worksheetCreator) {
     this.worksheetCreator = worksheetCreator;
  }

  public HttpClientFactory HttpClientFactory() {
     return httpClientFactory;
  }

  public void setHttpClientFactory(HttpClientFactory httpClientFactory) {
     this.httpClientFactory = httpClientFactory;
  }

  public ImageUtil getImageUtil() {
     return imageUtil;
  }

  public void setImageUtil(ImageUtil imageUtil) {
     this.imageUtil = imageUtil;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
     System.out.println("afterPropertiesSet WATCHDOG TASK IN GAE");
     if ( datastore == null ) throw new IllegalStateException("no datastore specified");
     if ( userService == null ) throw new IllegalStateException("no user service specified");
     if ( uploadSubmissions == null ) throw new IllegalStateException("no uploadSubmissions specified");
     if ( csvGenerator == null ) throw new IllegalStateException("no csvGenerator specified");
     if ( kmlGenerator == null ) throw new IllegalStateException("no kmlGenerator specified");
     if ( jsonFileGenerator == null ) throw new IllegalStateException("no jsonFileGenerator specified");
     if ( formDelete == null ) throw new IllegalStateException("no formDelete specified");
     if ( purgeSubmissions == null ) throw new IllegalStateException("no purgeSubmissions specified");
     if ( worksheetCreator == null ) throw new IllegalStateException("no worksheetCreator specified");
     if ( httpClientFactory == null ) throw new IllegalStateException("no httpClientFactory specified");
     if ( imageUtil == null ) throw new IllegalStateException("no imageUtil specified");
  }

  @Override
  public CallingContext getCallingContext() {
     return new CallingContextImpl();
  }

  @Override
  public void setFasterWatchdogCycleEnabled(boolean value) {
    lastFastPublishingEnabledFlag = value;
  }

  @Override
  public boolean getFasterWatchdogCycleEnabled() {
    return lastFastPublishingEnabledFlag;
  }
}

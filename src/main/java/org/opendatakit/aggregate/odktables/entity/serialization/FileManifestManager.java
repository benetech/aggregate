/*
 * Copyright (C) 2012-2013 University of Washington
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
package org.opendatakit.aggregate.odktables.entity.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.entity.OdkTablesFileManifestEntry;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.impl.api.FileServiceImpl;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.HtmlUtil;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Manages the manifest of files.
 * @author sudar.sam@gmail.com
 *
 */
public class FileManifestManager {
  
  private static final ObjectMapper MAPPER = new ObjectMapper();
  
  private String appId;
  private CallingContext cc;
  private Log log;
  
  public FileManifestManager(String appId, CallingContext cc) {
    this.appId = appId;
    this.cc = cc;
    this.log = LogFactory.getLog(FileManifestManager.class);
  }
  
  /**
   * Get the manifest entries for all the files associated with the app.
   * @return
   * @throws ODKDatastoreException 
   * @throws IOException 
   * @throws JsonMappingException 
   * @throws JsonGenerationException 
   */
  public String getManifestForAllAppFiles() throws ODKDatastoreException, 
      JsonGenerationException, JsonMappingException, IOException {
    // TODO: need to handle access control
    DbTableFiles dbTableFiles = new DbTableFiles(cc);
    List<Entity> entities = DbTableFileInfo.queryForAppId(appId, cc);
    List<OdkTablesFileManifestEntry> manifestEntries =
        getEntriesFromQuery(entities, dbTableFiles);
    String jsonManifest = MAPPER.writeValueAsString(manifestEntries);
    return jsonManifest;
  }
  
  
  
  /**
   * Get the manifest entries for the files associated with the given table.
   * @param tableId
   * @return
   * @throws ODKDatastoreException 
   * @throws IOException 
   * @throws JsonMappingException 
   * @throws JsonGenerationException 
   */
  public String getManifestForTable(String tableId) throws 
      ODKDatastoreException, JsonGenerationException, JsonMappingException, 
      IOException {
    // TODO: need to handle access control.
    DbTableFiles dbTableFiles = new DbTableFiles(cc);
    List<Entity> entities = DbTableFileInfo.queryForAppAndTable(appId, tableId, 
        cc);
    List<OdkTablesFileManifestEntry> manifestEntries =
        getEntriesFromQuery(entities, dbTableFiles);
    String jsonManifest = MAPPER.writeValueAsString(manifestEntries);
    return jsonManifest;
  }
  
  /**
   * Get the manifest entries for the application-level files--i.e. those that
   * are not associated with a certain table. This will return those with the
   * {@link FileServiceImpl#DEFAULT_TABLE_ID}.
   * @return
   * @throws ODKDatastoreException 
   * @throws IOException 
   * @throws JsonMappingException 
   * @throws JsonGenerationException 
   */
  public String getManifestForAppLevelFiles() throws ODKDatastoreException, 
      JsonGenerationException, JsonMappingException, IOException {
    // TODO: need to handle access control.
    DbTableFiles dbTableFiles = new DbTableFiles(cc);
    List<Entity> entities = DbTableFileInfo.queryForAppAndTable(appId, 
        FileServiceImpl.DEFAULT_TABLE_ID, cc);
    List<OdkTablesFileManifestEntry> manifestEntries =
        getEntriesFromQuery(entities, dbTableFiles);
    String jsonManifest = MAPPER.writeValueAsString(manifestEntries);
    return jsonManifest;    
  }
  
  /**
   * Get a list of entries from a query of {@link DbTableFileInfo}. The query
   * can be of any level--app, table, or even single entry.
   * @param entities
   * @param DbTableFiles 
   * @return
   * @throws ODKDatastoreException 
   */
  private List<OdkTablesFileManifestEntry> getEntriesFromQuery(
      List<Entity> entities, DbTableFiles dbTableFiles) throws 
      ODKDatastoreException {
    // TODO: need to handle access control.
    List<Row> infoRows = EntityConverter.toRowsFromFileInfo(entities);
    List<OdkTablesFileManifestEntry> manifestEntries = 
        new ArrayList<OdkTablesFileManifestEntry>();
    // A map of static url get parameters. In this case we only want to downoad
    // as an attachment.
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.AS_ATTACHMENT, "true");
    for (Row row : infoRows) {
      if (row.isDeleted()) {
        // We only want the non-deleted rows.
        continue;
      }
      OdkTablesFileManifestEntry entry = new OdkTablesFileManifestEntry();
      // To retrieve the actual file we need to get the uri of the file info 
      // row, which is the top-level uri of the blob tables holding the files.
      String rowUri = row.getRowId();
      String pathToFile = row.getValues().get(DbTableFileInfo.PATH_TO_FILE);
      BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(rowUri, cc);
      // We should only ever have one.
      if (blobEntitySet.getAttachmentCount(cc) > 1) {
        log.error("more than one entity for appId: " + appId + ", "
             + ", pathToFile: " + pathToFile);
      } else if (blobEntitySet.getAttachmentCount(cc) < 1) {
        log.error("file not found for: " + appId + ", pathToFile: " 
            + pathToFile);
        continue;
      }
      entry.filename = pathToFile;
      entry.md5hash = blobEntitySet.getContentHash(1, cc);
      String urlPartial = cc.getServerURL() + BasicConsts.FORWARDSLASH + 
          ServletConsts.ODK_TABLES_SERVLET_BASE_PATH + 
          BasicConsts.FORWARDSLASH +  FileService.SERVLET_PATH + 
          BasicConsts.FORWARDSLASH + pathToFile;
      String urlComplete = HtmlUtil.createLinkWithProperties(urlPartial, 
          properties);
      entry.downloadUrl = urlComplete;
      manifestEntries.add(entry);
    }
    return manifestEntries;
  }
  

}

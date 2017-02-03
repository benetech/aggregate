package org.opendatakit.aggregate.server;

import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

import java.util.ArrayList;

/**
 * Helper class for exporting data from ODK Tables to CSV and JSON files. It is responsible for
 * loading data from the database and holding credentials of the user which wants to export the data.
 */

public class ODKTablesExportHelper {

    private String appId;
    private String tableId;
    private CallingContext callingContext;
    private TablesUserPermissions tablesUserPermissions;
    private TableManager tableManager;
    private DataManager dataManager;
    private DataManager.WebsafeRows websafeRows;
    private ArrayList<String> userDefindedColumnNames;

    public ODKTablesExportHelper(String tableId, CallingContext callingContext) throws ODKEntityNotFoundException,
            ODKOverQuotaException, ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {
        this.tableId = tableId;
        this.callingContext = callingContext;
        this.appId = ServerPreferencesProperties.getOdkTablesAppId(callingContext);
        this.tablesUserPermissions = new TablesUserPermissionsImpl(callingContext);
        this.tableManager = new TableManager(appId, tablesUserPermissions, callingContext);
        this.dataManager = new DataManager(appId, tableId, tablesUserPermissions, callingContext);
    }

    /**
     * Loads user defined column names from the database.
     *
     * @return boolean if the load was performed successfully
     * @throws ODKDatastoreException
     * @throws PermissionDeniedException
     */
    public void loadUserDefinedColumnNames() throws ODKDatastoreException, PermissionDeniedException {
        TableEntry entry = tableManager.getTable(tableId);
        if ( entry == null || entry.getSchemaETag() == null ) {
            throw new ODKEntityNotFoundException();
        }
        this.userDefindedColumnNames = DbColumnDefinitions.queryForDbColumnNames(tableId,
                entry.getSchemaETag(), callingContext);
    }

    /**
     * Loads data rows from a particular table from the database.
     *
     * @return boolean if the data load was performed successfully
     * @throws ODKDatastoreException
     * @throws PermissionDeniedException
     * @throws ODKTaskLockException
     * @throws InconsistentStateException
     * @throws BadColumnNameException
     */
    public void loadWebsafeRows() throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException,
            InconsistentStateException, BadColumnNameException {
        // null - fetch data from the beginning
        // 0 - unlimited number of rows
        this.websafeRows = this.dataManager.getRows(null, 0);
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Gets all user defined column names
     *
     * @return ArrayList<String> of user defined column names
     */

    public ArrayList<String> getUserDefinedColumnNames() {
        return this.userDefindedColumnNames;
    }

    /**
     * Gets the wrapper class DataManager.WebsafeRows containing data rows
     *
     * @return DataManager.WebsafeRows which contains data rows
     */
    public DataManager.WebsafeRows getWebsafeRows() {
        return this.websafeRows;
    }


}

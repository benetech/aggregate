package org.opendatakit.aggregate.servlet;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ODKDefaultColumnNames;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvWriter;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.server.ODKTablesExportHelper;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Servlet for downloading a .csv file containing all submitted data for a particular form
 * stored within one ODK Table.
 */
public class OdkTablesExportTableContentToCSVServlet extends ServletUtilBase {

    private static final Log logger = LogFactory.getLog(OdkTablesExportTableContentToCSVServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        // Get the tableId of the table to be exported to the .csv file from the parameters
        String tableId = req.getParameter(ServletConsts.TABLE_ID);

        if (tableId == null) {
            errorMissingKeyParam(resp);
            return;
        }

        // Calling context
        CallingContext callingContext = ContextFactory.getCallingContext(this, req);

        // ODKTablesExportHelper is responsible for connecting to the database and loading
        // user defined column names and data rows from a particular table.
        ODKTablesExportHelper odkTablesExportHelper = null;
        try {
            odkTablesExportHelper = new ODKTablesExportHelper(tableId, callingContext);
            odkTablesExportHelper.loadUserDefinedColumnNames();
            odkTablesExportHelper.loadWebsafeRows();
        } catch (ODKDatastoreException e) {
            e.printStackTrace();
        } catch (PermissionDeniedException e) {
            e.printStackTrace();
        } catch (ODKTaskLockException e) {
            e.printStackTrace();
        } catch (BadColumnNameException e) {
            e.printStackTrace();
        } catch (InconsistentStateException e) {
            e.printStackTrace();
        }

        if (odkTablesExportHelper == null || odkTablesExportHelper.getWebsafeRows() == null || odkTablesExportHelper.getUserDefinedColumnNames() == null) {
            logger.error("Error while loading data from the database. No data loaded.");
            return;
        }

        if (req.getScheme().equals("http")) {
            logger.warn("Retrieving table content over http");
        }

        // Begin creation of the CSV file
        StringWriter buffer = new StringWriter();

        // Writes one row at a time with data separated with tab key
        RFC4180CsvWriter writer = new RFC4180CsvWriter(buffer);

        // Write column headings
        ArrayList<String> columnHeadings = new ArrayList<String>();
        columnHeadings.add(ODKDefaultColumnNames.DELETE_ROW_HEADING);
        for ( String columnName : odkTablesExportHelper.getUserDefinedColumnNames()) {
            columnHeadings.add(columnName);
        }
        columnHeadings.add(ODKDefaultColumnNames.SAVEPOINT_TYPE);
        columnHeadings.add(ODKDefaultColumnNames.FORM_ID);
        columnHeadings.add(ODKDefaultColumnNames.LOCALE);
        columnHeadings.add(ODKDefaultColumnNames.SAVEPOINT_TIMESTAMP);
        columnHeadings.add(ODKDefaultColumnNames.SAVEPOINT_CREATOR);
        columnHeadings.add(ODKDefaultColumnNames.ROW_ID);
        columnHeadings.add(ODKDefaultColumnNames.ROW_ETAG);
        columnHeadings.add(ODKDefaultColumnNames.FILTER_TYPE);
        columnHeadings.add(ODKDefaultColumnNames.FILTER_VALUE);
        columnHeadings.add(ODKDefaultColumnNames.LAST_UPDATE_USER);
        columnHeadings.add(ODKDefaultColumnNames.CREATED_BY_USER);
        columnHeadings.add(ODKDefaultColumnNames.DATA_ETAG_AT_MODIFICATION);

        writer.writeNext(columnHeadings.toArray(new String[0]));

        //Write all the data to the specific columns
        ArrayList<String> dataRow = new ArrayList<String>();
        for (Row row: odkTablesExportHelper.getWebsafeRows().rows) {
            // Is row deleted
            dataRow.add(String.valueOf(row.isDeleted()));

            // User defined column values
            for (DataKeyValue keyValue : row.getValues()) {
                dataRow.add(keyValue.value);
            }

            // Default columns
            dataRow.add(row.getSavepointType());
            dataRow.add(row.getFormId());
            dataRow.add(row.getLocale());
            dataRow.add(row.getSavepointTimestamp());
            dataRow.add(row.getSavepointCreator());
            dataRow.add(row.getRowId());
            dataRow.add(row.getRowETag());
            dataRow.add(row.getRowFilterScope().getType().name());
            dataRow.add(row.getRowFilterScope().getValue());
            dataRow.add(row.getLastUpdateUser());
            dataRow.add(row.getCreateUser());
            dataRow.add(row.getDataETagAtModification());

            writer.writeNext(dataRow.toArray(new String[0]));
            dataRow.clear();
        }

        // Close the writer of the .csv file content
        writer.close();

        // Prepare timestamp for the file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

        // Prepare response with a generated .csv file
        resp.setHeader("Cache-Control:", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma:", "no-cache");
        resp.setHeader("Expires:", "0");

        resp.setHeader("Last-Modified:", WebUtils.rfc1123Date(new Date()));
        resp.setContentType(HtmlConsts.RESP_TYPE_CSV);
        resp.addHeader(HtmlConsts.CONTENT_DISPOSITION, "attachment; filename=" + tableId + "_" + timeStamp + ".csv");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getOutputStream().write(buffer.getBuffer().toString().getBytes(CharEncoding.UTF_8));
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }
}
package org.opendatakit.aggregate.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.server.ODKTablesExportHelper;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Servlet for downloading a .json file containing all submitted data for a particular form
 * stored within one ODK Table.
 */
public class OdkTablesExportTableContentToJSONServlet extends ServletUtilBase {

    private static final Log logger = LogFactory.getLog(OdkTablesExportTableContentToJSONServlet.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // Get the tableId of the table to be exported to the .json file from the parameters
        String tableId = req.getParameter(ServletConsts.TABLE_ID);

        if (tableId == null) {
            errorMissingKeyParam(resp);
            return;
        }

        // Calling context
        CallingContext callingContext = ContextFactory.getCallingContext(this, req);

        // ODKTablesExportHelper is responsible for connecting to the database and loading
        // data rows from a particular table.
        ODKTablesExportHelper odkTablesExportHelper = null;
        try {
            odkTablesExportHelper = new ODKTablesExportHelper(tableId, callingContext);
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

        if (odkTablesExportHelper == null || odkTablesExportHelper.getWebsafeRows() == null) {
            logger.error("Error while loading data from the database. No data loaded.");
            return;
        }

        if (req.getScheme().equals("http")) {
            logger.warn("Retrieving table content over http");
        }

        // Prepare timestamp for the file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

        // Convert the data from rows to .json
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().create();
        String parsedJson = gson.toJson(odkTablesExportHelper.getWebsafeRows());

        FileWriter writer = new FileWriter(tableId+"_"+timeStamp+".json", false);
        writer.write(parsedJson);
        writer.close();

        // Prepare response with a generated .json file
        resp.setHeader("Cache-Control:", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma:", "no-cache");
        resp.setHeader("Expires:", "0");

        resp.setHeader("Last-Modified:",
                WebUtils.rfc1123Date(new Date()));
        resp.setContentType(HtmlConsts.RESP_TYPE_JSON);
        resp.addHeader(HtmlConsts.CONTENT_DISPOSITION, "attachment; filename=" + tableId + "_" + timeStamp + ".json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getOutputStream().write(parsedJson.getBytes(CharEncoding.UTF_8));
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }
}
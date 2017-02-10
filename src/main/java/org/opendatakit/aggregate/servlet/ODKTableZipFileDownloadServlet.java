package org.opendatakit.aggregate.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.odktables.relation.DbTableInstanceFiles;
import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentRefBlob;
import org.opendatakit.common.datamodel.RefBlob;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Servlet for downloading a .zip file containing all files related to a particular form
 * instances stored within one ODK Table.
 */
public class ODKTableZipFileDownloadServlet extends ServletUtilBase {

    private static final Log logger = LogFactory.getLog(ODKTableZipFileDownloadServlet.class);

    // Used to create the names of tables storing blobs within the database
    private static final String TABLE_PREFIX = "___";
    private static final String TABLE_REF_SUFFIX = "_att_ref";
    private static final String TABLE_BLB_SUFFIX = "_att_blb";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        // Get the tableId of the table from which files should be exported to .zip file
        String tableId = req.getParameter(ServletConsts.TABLE_ID);

        if (tableId == null) {
            errorMissingKeyParam(resp);
            return;
        }

        // Calling context
        CallingContext cc = ContextFactory.getCallingContext(this, req);

        // List of files associated with a certain table
        List<BinaryContent> contents = null;
        try {
            DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
            contents = blobStore.getAllBinaryContents(cc);
        } catch (ODKDatastoreException e) {
            e.printStackTrace();
        }

        // Prepare response containing a zip file
        ServletOutputStream sos = resp.getOutputStream();
        try {
            if (contents != null) {

                // Call the zipFiles method for creating a zip stream
                byte[] zip = zipFiles(contents, cc, tableId);

                // Prepare timestamp for the file name
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

                resp.setContentType("application/zip");
                resp.setHeader("Content-Disposition", "attachment; filename=" + tableId + "_" + timeStamp + ".zip");

                sos.write(zip);
                sos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] zipFiles(List<BinaryContent> contents, CallingContext callingContext, String tableId) throws IOException, ODKDatastoreException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        try {
            String schemaName = callingContext.getDatastore().getDefaultSchemaName();
            String tableName = tableId.toLowerCase();

            for (BinaryContent binaryContent : contents) {
                // Create file with the original file name
                zos.putNextEntry(new ZipEntry(binaryContent.getUnrootedFilePath()));

                // Create BlobManipulator to read the blob from the database
                BinaryContentManipulator.BlobManipulator blobManipulator = new BinaryContentManipulator.BlobManipulator(
                        binaryContent.getUri(),
                        new BinaryContentRefBlob(schemaName, TABLE_PREFIX + tableName + TABLE_REF_SUFFIX),
                        new RefBlob(schemaName, TABLE_PREFIX + tableName + TABLE_BLB_SUFFIX),
                        callingContext
                );

                // Read blob
                zos.write(blobManipulator.getBlob());
                zos.closeEntry();
            }

            zos.finish();
            baos.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zos.close();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return baos.toByteArray();
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }
}
package org.opendatakit.aggregate.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbTableInstanceFiles;
import org.opendatakit.aggregate.server.ODKTablesExportHelper;
import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentRefBlob;
import org.opendatakit.common.datamodel.RefBlob;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
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

public class OdkTablesExportTableContentServlet extends ServletUtilBase {

    private static final Log logger = LogFactory.getLog(OdkTablesExportTableContentServlet.class);

    // Used to create the names of tables storing blobs within the database
    private static final String TABLE_PREFIX = "___";
    private static final String TABLE_REF_SUFFIX = "_att_ref";
    private static final String TABLE_BLB_SUFFIX = "_att_blb";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        String tableId = req.getParameter(ServletConsts.TABLE_ID);
        String fileFormat = req.getParameter(ServletConsts.FILE_TYPE);

        if (tableId == null || fileFormat == null) {
            errorMissingKeyParam(resp);
            return;
        }

        CallingContext callingContext = ContextFactory.getCallingContext(this, req);
        ODKTablesExportHelper odkTablesExportHelper = null;

         try {
             odkTablesExportHelper = new ODKTablesExportHelper(tableId, callingContext);
        } catch (ODKDatastoreException e) {
            e.printStackTrace();
        } catch (PermissionDeniedException e) {
            e.printStackTrace();
        } catch (ODKTaskLockException e) {
            e.printStackTrace();
        }

        if (req.getScheme().equals("http")) {
            logger.warn("Retrieving table content over http");
        }

        // Load all submitted instances from a specific ODK Table
        byte[] tableEntries = null;

        if(fileFormat.equals(ServletConsts.JSON)) {
            logger.info("Exporting table: " + tableId + " entries to JSON file");
            tableEntries = odkTablesExportHelper.exportData(ServletConsts.JSON);
        }
        if(fileFormat.equals(ServletConsts.CSV)) {
            logger.info("Exporting table: " + tableId + " entries to CSV file");
            tableEntries = odkTablesExportHelper.exportData(ServletConsts.CSV);
        }

        // List of files (blobs) associated with a certain table
        List<BinaryContent> contents = null;
        try {
            DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, callingContext);
            contents = blobStore.getAllBinaryContents(callingContext);
        } catch (ODKDatastoreException e) {
            e.printStackTrace();
        }

        // Prepare response containing a ZIP file
        ServletOutputStream sos = resp.getOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            if (contents == null && tableEntries == null) {
                logger.error("Unrecognised file format or failed file preparation");
            } else {
                logger.info("Compressing table: " + tableId + " data to the archive file");

                // Prepare timestamp
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

                // Zip blob files associated with the table
                zipBlobFiles(zos, contents, callingContext, tableId);

                // Create name for a CSV/JSON file
                String fileName = null;
                if(fileFormat.equals(ServletConsts.JSON)) {
                    fileName = tableId + "_" + timeStamp + ".json";
                }
                if(fileFormat.equals(ServletConsts.CSV)) {
                    fileName = tableId + "_" + timeStamp + ".csv";
                }

                // Zip CSV/JSON file containing all instances of a particular survey from an ODK Table
                zipFile(zos, fileName, tableEntries);

                zos.finish();
                baos.flush();

                resp.setContentType("application/zip");
                resp.setHeader("Content-Disposition", "attachment; filename=" + tableId + "_" + timeStamp + ".zip");

                sos.write(baos.toByteArray());
                sos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zos.close();
                baos.close();
                sos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void zipBlobFiles(ZipOutputStream zos, List<BinaryContent> contents, CallingContext callingContext, String tableId) throws IOException, ODKDatastoreException {

        try {
            String schemaName = callingContext.getDatastore().getDefaultSchemaName();
            String tableName = tableId.toLowerCase();

            for (BinaryContent binaryContent : contents) {

                // Create BlobManipulator to read the blob from the database
                BinaryContentManipulator.BlobManipulator blobManipulator = new BinaryContentManipulator.BlobManipulator(
                        binaryContent.getUri(),
                        new BinaryContentRefBlob(schemaName, TABLE_PREFIX + tableName + TABLE_REF_SUFFIX),
                        new RefBlob(schemaName, TABLE_PREFIX + tableName + TABLE_BLB_SUFFIX),
                        callingContext
                );

                //Create file with the original file name and
                zipFile(zos, binaryContent.getUnrootedFilePath(), blobManipulator.getBlob());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zipFile(ZipOutputStream zos, String fileName, byte[] fileContent) throws IOException {
        logger.info("Compressing file: " + fileName);
        zos.putNextEntry(new ZipEntry(fileName));
        zos.write(fileContent);
        zos.closeEntry();
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }
}

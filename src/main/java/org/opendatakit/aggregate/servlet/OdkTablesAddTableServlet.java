package org.opendatakit.aggregate.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.ConfigFileChangeDetail;
import org.opendatakit.aggregate.odktables.FileContentInfo;
import org.opendatakit.aggregate.odktables.FileManager;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.impl.api.ServiceUtils;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Servlet that handles the generation of a table from an uploaded JSON file.
 */
public class OdkTablesAddTableServlet extends ServletUtilBase {

    /**
     * Serial number for serialization
     */
    private static final long serialVersionUID = 2019527279345557770L;
    /**
     * URI from base
     */
    public static final String ADDR = UIConsts.ADD_TABLE_FROM_ZIP_SERVLET_ADDR;

    /**
     * Title for generated webpage
     */
    private static final String TITLE_INFO = "ODK Tables Form Upload";

    private static final String UPLOAD_PAGE_BODY_START =

            "<div style=\"overflow: auto;\"><p id=\"subHeading\"><h2>Upload one form into ODK Tables</h2></p>"
                    + "<!--[if true]><p style=\"color: red;\">For a better user experience, use Chrome, Firefox or Safari</p>"
                    + "<![endif] -->"
                    + "<form id=\"ie_backward_compatible_form\""
                    + " accept-charset=\"UTF-8\" method=\"POST\" encoding=\"multipart/form-data\" enctype=\"multipart/form-data\""
                    + " action=\"";// emit the ADDR

    private static final String UPLOAD_PAGE_BODY_MIDDLE = "\">"
            + "	  <table id=\"uploadTable\">"
            + "	  	<tr>"
            + "	  		<td><label for=\"form_zip\">Form files (ZIP file):</label></td>"
            + "	  		<td><input id=\"form_zip\" type=\"file\" size=\"80\" class=\"gwt-Button\""
            + "	  			name=\"form_zip\" /></td>"
            + "	  	</tr>\n"
            + "	  	<tr>"
            + "	  		<td><input id=\"upload_form\" type=\"submit\" name=\"button\" class=\"gwt-Button\" value=\"Upload Form\" /></td>"
            + "	  		<td />"
            + "	  	</tr>"
            + "	  </table>\n"
            + "	  </form>"
            + "<p>Form should be in a single zip file with structure that is consistent with requirements for Survey app. <br/>"
            + "If form of the same name is already on server, you should remove it before upload.</p>";

    private static final Log logger = LogFactory.getLog(OdkTablesAddTableServlet.class);

    /**
     * Handler for HTTP Get request to create ODK Tables form upload page
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServiceUtils.examineRequest(getServletContext(), req);

        CallingContext cc = ContextFactory.getCallingContext(this, req);

        StringBuilder headerString = new StringBuilder();
        headerString.append("<script type=\"application/javascript\" src=\"");
        headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_SCRIPT_RESOURCE));
        headerString.append("\"></script>");
        headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_STYLE_RESOURCE));
        headerString.append("\" />");
        headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_BUTTON_STYLE_RESOURCE));
        headerString.append("\" />");
        headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        headerString.append(cc.getWebApplicationURL(ServletConsts.AGGREGATE_STYLE));
        headerString.append("\" />");

        // header info
        beginBasicHtmlResponse(TITLE_INFO, headerString.toString(), resp, cc);
        addOpenDataKitHeaders(resp);
        PrintWriter out = resp.getWriter();
        out.write(UPLOAD_PAGE_BODY_START);
        out.write(cc.getWebApplicationURL(ADDR));
        out.write(UPLOAD_PAGE_BODY_MIDDLE);
        finishBasicHtmlResponse(resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServiceUtils.examineRequest(getServletContext(), req);
        @SuppressWarnings("unused")
        CallingContext cc = ContextFactory.getCallingContext(this, req);
        logger.info("Inside doHead");
        addOpenDataKitHeaders(resp);
        resp.setStatus(204); // no content...
    }

    /**
     * Handler for HTTP Post request that takes a JSON file, uses that file to add
     * a new OdkTables table to the datastore.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServiceUtils.examineRequest(getServletContext(), req);
        CallingContext cc = ContextFactory.getCallingContext(this, req);

        req.getContentLength();
        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_MULTI_PART_CONTENT);
            return;
        }

        try {
            String appId = ContextFactory.getOdkTablesAppId(cc);
            TablesUserPermissions userPermissions = ContextFactory.getTablesUserPermissions(cc);
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
            Map<String,byte[]> files = new HashMap<>();
            String definition = null;
            String tableId = null;

            // unzipping files

            for (FileItem item : items) {
                String fieldName = item.getFieldName();
                String fileName = FilenameUtils.getName(item.getName());

                if (fieldName.equals("form_zip")) {

                    if (!(fileName.endsWith(".zip"))) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_ZIP_FILE);
                        return;
                    }

                    InputStream fileStream = item.getInputStream();
                    ZipInputStream zipStream = new ZipInputStream(fileStream);

                    int c;

                    byte buffer[] = new byte[2084];
                    ByteArrayOutputStream tempBAOS;
                    ZipEntry zipEntry;
                    while ((zipEntry = zipStream.getNextEntry()) != null) {
                        if (!(zipEntry.isDirectory())) {
                            tempBAOS = new ByteArrayOutputStream();
                            while ((c = zipStream.read(buffer,0,2048)) > -1) {
                                tempBAOS.write(buffer, 0, c);
                            }
                            files.put("tables" + BasicConsts.FORWARDSLASH + zipEntry.getName(),tempBAOS.toByteArray());
                            if (zipEntry.getName().endsWith("definition.csv")) {
                                tableId = FileManager.getTableIdForFilePath("tables" + BasicConsts.FORWARDSLASH + zipEntry.getName());
                                definition = new String(tempBAOS.toByteArray());
                            }
                        }
                    }
                }
            }

            if (definition == null || tableId == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_DEFINITION_FILE);
                return;
            }

            List<String> notUploadedFiles = new ArrayList<>();
            List<String> uploadedFiles = new ArrayList<>();

            //adding table
            List<Column> columns = parseColumnsFromCsv(definition);
            TableManager tm = new TableManager(appId,userPermissions,cc);
            tm.createTable(tableId,columns);


            //uploading files
            for (Map.Entry<String,byte[]> entry : files.entrySet()) {

                String contentType = ServletConsts.MIME_TYPES.get(entry.getKey().substring(entry.getKey().lastIndexOf(".")+1));
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                FileManager fm = new FileManager(appId, cc);
                FileContentInfo fi = new FileContentInfo(entry.getKey(), contentType,
                        Long.valueOf(entry.getValue().length), null, entry.getValue());

                ConfigFileChangeDetail outcome = fm.putFile("2", tableId, fi, userPermissions);

                if(outcome==ConfigFileChangeDetail.FILE_NOT_CHANGED) {
                    notUploadedFiles.add(entry.getKey());
                } else {
                    uploadedFiles.add(entry.getKey());
                }
            }

            //sending outcome
            PrintWriter out = resp.getWriter();
            if (notUploadedFiles.isEmpty()) {
                out.write("<p>Table " + tableId + " and all of it's files was uploaded successfully. Files uploaded:");
                for (String file : uploadedFiles) {
                    out.write("<br/>" + file);
                }
                out.write("</p>");
            } else {
                out.write("<p>Some files for table " + tableId + " was not uploaded successfully. Files uploaded:");
                for (String file : uploadedFiles) {
                    out.write("<br/>" + file);
                }
                out.write("</br></br>Files not uploaded:");
                for (String file : notUploadedFiles) {
                    out.write("<br/>" + file);
                }
                out.write("</p>");
            }

        } catch (FileUploadException e) {
            logger.error("error uploading zip: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
        } catch (ODKOverQuotaException e) {
            logger.error("error uploading zip: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
        } catch (ODKEntityNotFoundException e) {
            logger.error("error uploading zip: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
        } catch (ODKDatastoreException e) {
            logger.error("error uploading zip: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
        } catch (ODKTaskLockException e) {
            logger.error("error uploading zip: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
        } catch (PermissionDeniedException e) {
            logger.error("error uploading zip: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
        } catch (TableAlreadyExistsException e) {
            logger.error("error uploading zip: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
        }
    }

    private List<Column> parseColumnsFromCsv(String definition) {
        List<Column> outcome = new ArrayList<>();
        String columnsStrings[] = definition.split("\\s+");
        Column temp;
        for(String column : columnsStrings) {
            if (column.startsWith("_")) {
                continue;
            }
            String fields[] = column.split(",",4);
            temp = new Column(fields[0],fields[1],fields[2],fields[3].replaceAll("\"\"","\"").replaceAll("\\]\"","\\]").replaceAll("\"\\[","\\["));
            outcome.add(temp);
        }

        return outcome;
    }

}
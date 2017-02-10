package org.opendatakit.aggregate.client.popups;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.TableContentsClient;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.EnumListBox;
import org.opendatakit.aggregate.constants.common.ExportTableType;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import java.util.ArrayList;


public class ExportTablePopup extends AbstractPopupBase {

    private static final String FILE_TYPE_TOOLTIP = "Type of File to Generate";
    private static final String FILE_TYPE_BALLOON = "Select the type of file you wish to create.";

    private static final String CREATE_BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Download file";
    private static final String CREATE_BUTTON_TOOLTIP = "Create Export File";
    private static final String CREATE_BUTTON_HELP_BALLOON = "This creates a ZIP file containing either a CSV or JSON file of your data and all instance related files.";


    // this will be the standard header across the top
    private final FlexTable optionsBar;
    private final EnumListBox<ExportTableType> fileType;

    private final AggregateButton exportButton;

    private final String tableId;

    private String resumeCursor;
    private boolean hasMore;

    // that table's rows
    private ArrayList<RowClient> rows;

    // that table's column names
    private ArrayList<String> columnNames;

    public ExportTablePopup(String tableid) {
        super();
        this.tableId = tableid;

        exportButton = new AggregateButton(CREATE_BUTTON_TXT, CREATE_BUTTON_TOOLTIP,
                CREATE_BUTTON_HELP_BALLOON);
        exportButton.addClickHandler(new ExportTablePopup.CreateExportHandler());

        fileType = new EnumListBox<ExportTableType>(ExportTableType.values(), FILE_TYPE_TOOLTIP,
                FILE_TYPE_BALLOON);

        // set the standard header widgets
        optionsBar = new FlexTable();
        optionsBar.addStyleName("stretch_header");
        optionsBar.setWidget(0, 0, new HTML("<h2> Table Id:</h2>"));
        optionsBar.setWidget(0, 1, new HTML("<h3>" + tableId + "</h3>"));
        optionsBar.setWidget(0, 2, new HTML("<h2>Type:</h2>"));
        optionsBar.setWidget(0, 3, fileType);
        optionsBar.setWidget(0, 4, exportButton);
        optionsBar.setWidget(0, 5, new ClosePopupButton(this));
        setWidget(optionsBar);
    }

    // set up the callback object
    AsyncCallback<TableContentsClient> getDataCallbackCSV =
        new AsyncCallback<TableContentsClient>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof EntityNotFoundExceptionClient) {
                } else if (caught instanceof PermissionDeniedExceptionClient) {
                    // do nothing, b/c it's probably legitimate that you don't get an
                    // error if there are rows you're not allowed to see.
                } else {
                    AggregateUI.getUI().reportError(caught);
                }
            }

            @Override
            public void onSuccess(TableContentsClient tcc) {
                columnNames = tcc.columnNames;
                resumeCursor = tcc.websafeResumeCursor;
                hasMore = tcc.hasMore;
                rows = tcc.rows;

                String linkHref = UIConsts.EXPORT_TABLE_CONTENT_SERVLET_ADDR + "?tableId=" + tableId + "&fileType=CSV";
                Window.Location.replace(linkHref);
            }
        };

    // set up the callback object
    AsyncCallback<TableContentsClient> getDataCallbackJSON = new AsyncCallback<TableContentsClient>() {
        @Override
        public void onFailure(Throwable caught) {
            AggregateUI.getUI().reportError(caught);
        }

        @Override
        public void onSuccess(TableContentsClient tcc) {
            columnNames = tcc.columnNames;
            resumeCursor = tcc.websafeResumeCursor;
            hasMore = tcc.hasMore;
            rows = tcc.rows;

            String linkHref = UIConsts.EXPORT_TABLE_CONTENT_SERVLET_ADDR + "?tableId=" + tableId+ "&fileType=JSON";
            Window.Location.replace(linkHref);
        }
    };

    private class CreateExportHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            ExportTableType type = ExportTableType.valueOf(fileType.getValue(fileType.getSelectedIndex()));

            if (type == ExportTableType.CSV) {
                if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
                        .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
                    SecureGWT.getServerDataService().getTableContents(tableId, resumeCursor, getDataCallbackCSV);
                }
            } else if (type == ExportTableType.JSONFILE) {
                if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
                        .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
                    SecureGWT.getServerDataService().getTableContents(tableId, resumeCursor, getDataCallbackJSON);
                }
            } else {
                new ExportTablePopup.ErrorDialog().show();
            }
        }
    }

    private static class ErrorDialog extends DialogBox {

        public ErrorDialog() {
            setText("Error Unknown Export Type!! Please file an Issue on ODK website!");
            Button ok = new Button("OK");
            ok.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    ExportTablePopup.ErrorDialog.this.hide();
                }
            });
            setWidget(ok);
        }
    }
}

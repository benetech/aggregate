/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.table;

import static org.opendatakit.aggregate.client.table.TableFileInfoColumnType.CONTENT_LENGTH;
import static org.opendatakit.aggregate.client.table.TableFileInfoColumnType.CONTENT_TYPE;
import static org.opendatakit.aggregate.client.table.TableFileInfoColumnType.DELETE;
import static org.opendatakit.aggregate.client.table.TableFileInfoColumnType.DOWNLOAD;
import static org.opendatakit.aggregate.client.table.TableFileInfoColumnType.FILENAME;
import static org.opendatakit.aggregate.client.table.TableFileInfoColumnType.ODK_CLIENT_VERSION;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.AggregateSubTabBase;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.TableContentsForFilesClient;
import org.opendatakit.aggregate.client.widgets.OdkTablesDeleteAppLevelFileButton;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays the entries in the DbTableFileInfo table that pertain to
 * the application and not to any specific table.
 *
 * Based upon OdkTablesViewTableFileInfo
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesViewAppLevelFileInfo extends SortableFlexTable {

  // the table whose info we are currently displaying.
//  private TableEntryClient currentTable;

  // that table's info rows
  private ArrayList<FileSummaryClient> fileSummaries;

  private static final int NUMBER_COLUMNS = 6;

  // this is just the tab that opened the table
  private AggregateSubTabBase basePanel;

  // the message to display when there are no rows in the table
  private static String NO_ROWS_MESSAGE = "There are no rows to display.";

  public OdkTablesViewAppLevelFileInfo(AggregateSubTabBase tableSubTab) {
    setNumberColumns(NUMBER_COLUMNS);
    setUpdateColumns(true);
    setColumnHeadings();

    // add styling
    addStyleName("dataTable");
    getElement().setId("form_management_table");
    

    this.basePanel = tableSubTab;
  }

  @Override
  public void updateDisplay() {
    setUpdateColumns(false);
    updateData();    
  }
  
  public void updateData() {
    // set up the callback object
    AsyncCallback<TableContentsForFilesClient> getDataCallback = new AsyncCallback<TableContentsForFilesClient>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(TableContentsForFilesClient tcc) {
        
        if (isUpdateColumns()) {
          removeAllRows();
          setColumnHeadings();
        }
        fileSummaries = tcc.files;
        setRows();
      }
    };
    
    // Only issue the request if we have access...
    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
      SecureGWT.getServerDataService().getAppLevelFileInfoContents(getSortColumn(), isAscending(), getDataCallback);
    }
  }

  private void setColumnHeadings() {
    // create the table headers.
    setText(0,DELETE.getDisplayColumn(),  DELETE.getDisplayText());
    setWidget(0, ODK_CLIENT_VERSION.getDisplayColumn(), getClickableColumnHeading(ODK_CLIENT_VERSION.getDisplayText(),
        ODK_CLIENT_VERSION.getDbColumn()));
    setWidget(0, FILENAME.getDisplayColumn(), getClickableColumnHeading(FILENAME.getDisplayText(),
        FILENAME.getDbColumn()));
    setWidget(0, CONTENT_LENGTH.getDisplayColumn(), getClickableColumnHeading(CONTENT_LENGTH.getDisplayText(),
        CONTENT_LENGTH.getDbColumn()));
    setWidget(0, CONTENT_TYPE.getDisplayColumn(), getClickableColumnHeading(CONTENT_TYPE.getDisplayText(),
        CONTENT_TYPE.getDbColumn()));
    setText(0, DOWNLOAD.getDisplayColumn(), DOWNLOAD.getDisplayText());

    getRowFormatter().addStyleName(0, "titleBar");
  }

  /**
   * Attach code to sort column when heading is clicked.
   * 
   * @param columnName
   * @return
   */
  SortableColumnLabel getClickableColumnHeading(String columnLabel, String columnName) {
    SortableColumnLabel result = new SortableColumnLabel(columnLabel, columnName);
    result.setStyleName("odkTablesClickableColumnHeading");
    result.addClickHandler(new ColumnSortHandler(this, result));
    return result;
  }
  
  /*
   * This will set the row values in the listbox.
   */
  private void setRows() {
    int start = 1; // b/c the 0 row is the headings.

    int currentRow = start;
    // Window.alert(Integer.toString(rows.size()));
    // if there are no columns, then we only want to display the no data
    // message.
    // otherwise check if there are no rows.
    if (fileSummaries.size() == 0) {
      setWidget(currentRow, 0, new HTML(NO_ROWS_MESSAGE));
      // make the display fill all the columns you have. this is the total
      // number of
      // user-defined columns +1 for the delete column.
      this.getFlexCellFormatter().setColSpan(1, 0, getNumberColumns());
    } else { // there are rows--display them.

      for (int j = 0; j < fileSummaries.size(); j++) {
        FileSummaryClient sum = fileSummaries.get(j);
        OdkTablesDeleteAppLevelFileButton deleteButton = 
            new OdkTablesDeleteAppLevelFileButton(this.basePanel,
                sum.getOdkClientVersion(), sum.getFilename());
        if ( !AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES)) {
          deleteButton.setEnabled(false);
        }
        setWidget(currentRow, DELETE.getDisplayColumn(), deleteButton);
        setText(currentRow, ODK_CLIENT_VERSION.getDisplayColumn(), sum.getOdkClientVersion());
        setText(currentRow, FILENAME.getDisplayColumn(), sum.getFilename());
        getFlexCellFormatter().setStyleName(currentRow, FILENAME.getDisplayColumn(), "dataLeft");
        setText(currentRow, CONTENT_LENGTH.getDisplayColumn(), sum.getContentLength().toString());
        getFlexCellFormatter().setStyleName(currentRow, CONTENT_LENGTH.getDisplayColumn(), "dataRight");
        setText(currentRow, CONTENT_TYPE.getDisplayColumn(), sum.getContentType());
        Widget downloadCol;
        if (sum.getDownloadUrl() != null) {
          Anchor downloadLink = new Anchor();
          downloadLink.setText("Get");
          downloadLink.setHref(sum.getDownloadUrl());
          downloadCol = downloadLink;
        } else {
          downloadCol = new HTML("");
        }
        setWidget(currentRow, DOWNLOAD.getDisplayColumn(), downloadCol);
        if (currentRow % 2 == 0) {
          getRowFormatter().addStyleName(currentRow, "evenTableRow");
        }
        currentRow++;
      }
      while (getRowCount() > currentRow) {
        removeRow(getRowCount() - 1);
      }
    }
  }



}

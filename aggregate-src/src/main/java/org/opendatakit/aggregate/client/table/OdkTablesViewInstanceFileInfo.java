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

import static org.opendatakit.aggregate.client.table.InstanceFileInfoColumnType.CONTENT_LENGTH;
import static org.opendatakit.aggregate.client.table.InstanceFileInfoColumnType.CONTENT_TYPE;
import static org.opendatakit.aggregate.client.table.InstanceFileInfoColumnType.DELETE;
import static org.opendatakit.aggregate.client.table.InstanceFileInfoColumnType.DOWNLOAD;
import static org.opendatakit.aggregate.client.table.InstanceFileInfoColumnType.FILENAME;
import static org.opendatakit.aggregate.client.table.InstanceFileInfoColumnType.INSTANCE_ID;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.AggregateSubTabBase;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.OdkTablesManageTableFilesSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.TableContentsForFilesClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.widgets.OdkTablesDeleteInstanceFileButton;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays the files associated with individual rows in a table.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesViewInstanceFileInfo extends SortableFlexTable {

  // the table whose info we are currently displaying.
  private TableEntryClient currentTable;

  // that table's info rows
  private ArrayList<FileSummaryClient> fileSummaries;


  private static final int NUMBER_COLUMNS = 6;

  // this is just the tab that opened the table
  private AggregateSubTabBase basePanel;

  // the message to display when there are no rows in the table
  private static String NO_ROWS_MESSAGE = "There are no rows to display.";

  /**
   * This is the constructor to call when there has not been a table selected.
   * Should this even exist?
   */
  public OdkTablesViewInstanceFileInfo(AggregateSubTabBase tableSubTab) {

    setNumberColumns(NUMBER_COLUMNS);
    setColumnHeadings();

    // add styling
    addStyleName("dataTable");
    getElement().setId("form_management_table");

    this.basePanel = tableSubTab;

    // no current table.
    this.currentTable = null;
  }

  public OdkTablesViewInstanceFileInfo(AggregateSubTabBase tableSubTab, TableEntryClient table) {
    this(tableSubTab);

    updateDisplay(table);

    this.currentTable = table;
  }

  @Override
  public void updateDisplay() {
    updateDisplay(this.currentTable);
  }

  
  /**
   * This updates the display to show the contents of the table.
   */
  public void updateDisplay(TableEntryClient table) {
    @SuppressWarnings("unused")
    TableEntryClient oldTable = this.currentTable;

    if (oldTable == null || table == null || !oldTable.getTableId().equals(table.getTableId())) {
      setUpdateColumns(true);
    } else {
      setUpdateColumns(false);
    }
    this.currentTable = table;

    if (table == null) {
      this.removeAllRows();
    } else {

      /*** update the data ***/
      updateData(table);
    }

  }

  public void updateData(TableEntryClient table) {
    // set up the callback object
    AsyncCallback<TableContentsForFilesClient> getDataCallback = new AsyncCallback<TableContentsForFilesClient>() {
      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof EntityNotFoundExceptionClient) {
          // if this happens it is PROBABLY, but not necessarily, because
          // we've deleted the table.
          // TODO ensure the correct exception makes it here
          ((OdkTablesManageTableFilesSubTab) AggregateUI.getUI().getSubTab(SubTabs.MANAGE_TABLE_ID_FILES))
              .setTabToDisplayZero();
        } else {
          AggregateUI.getUI().reportError(caught);
        }
      }

      @Override
      public void onSuccess(TableContentsForFilesClient tcc) {
        
        if (isUpdateColumns()) {
          removeAllRows();
          setColumnHeadings();
        }

        fileSummaries = tcc.files;
        setRows();

        // AggregateUI.getUI().getTimer().refreshNow();
      }
    };

    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
      SecureGWT.getServerDataService().getInstanceFileInfoContents(table.getTableId(), getSortColumn(), isAscending(), getDataCallback);
    }
  }

  private void setColumnHeadings() {
    // create the table headers.
    setText(0,DELETE.getDisplayColumn(),  DELETE.getDisplayText());
    setWidget(0, INSTANCE_ID.getDisplayColumn(), getClickableColumnHeading(INSTANCE_ID.getDisplayText(),
        INSTANCE_ID.getDbColumn()));
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
        String filename = sum.getFilename();
        String instanceId = sum.getInstanceId();
        OdkTablesDeleteInstanceFileButton deleteButton =
                new OdkTablesDeleteInstanceFileButton(this.basePanel,
                        currentTable.getTableId(), instanceId, sum.getDownloadUrl());
        if ( !AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES)) {
          deleteButton.setEnabled(false);
        }
        setWidget(currentRow, DELETE.getDisplayColumn(), deleteButton);
        setText(currentRow, INSTANCE_ID.getDisplayColumn(), instanceId);
        setText(currentRow, FILENAME.getDisplayColumn(), filename);
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

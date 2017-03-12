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

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.OdkTablesViewTableSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.TableContentsClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.widgets.OdkTablesAdvanceRowsButton;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Displays the contents of a table.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesViewTable extends CellTable<RowClientWrapper> {

  // the table that we are currently displaying.
  private TableEntryClient currentTable;

  // that table's rows
  private List<RowClientWrapper> rows;

  // that table's column names
  private List<String> columnNames;

  private String refreshCursor;
  private String resumeCursor;
  private boolean hasMore;

  private OdkTablesAdvanceRowsButton tableAdvanceButton;

  // Number of rows for pagination
  private static final int PAGE_SIZE = 4;

  // the message to display when there is no data in the table.
  private static String NO_DATA_MESSAGE = "There is no data in this table.";
  // the message to display when there are no rows in the table
  private static String NO_ROWS_MESSAGE = "There are no rows to display.";

  /**
   * This is the constructor to call when there has not been a table selected.
   * Should this even exist?
   */
  public OdkTablesViewTable() {
    // add styling
    //super(PAGE_SIZE, GWT.<CellTableStyleResource> create(CellTableStyleResource.class));
    super(PAGE_SIZE);
    this.addStyleName("dataTable");

    getElement().setId("form_management_table");

    // no current table.
    this.currentTable = null;
    this.refreshCursor = null;
    this.resumeCursor = null;
    this.hasMore = false;
  }

  public OdkTablesViewTable(TableEntryClient table) {
    this();

    updateDisplay(table);

    this.currentTable = table;
  }

  public void setAdvanceButton(OdkTablesAdvanceRowsButton tableAdvanceButton) {
    this.tableAdvanceButton = tableAdvanceButton;
    if (this.tableAdvanceButton != null) {
      this.tableAdvanceButton.setEnabled(hasMore);
    }
  }

  /**
   * This updates the display to show the contents of the table.
   */
  public void updateDisplay(TableEntryClient table) {
    TableEntryClient oldTable = this.currentTable;

    // for testing timing
    // Window.alert("in odktablesViewTable.updateDisplay()");

    this.currentTable = table;

    if (oldTable == null || currentTable == null
        || !oldTable.getTableId().equals(currentTable.getTableId())) {
      this.refreshCursor = null;
      this.resumeCursor = null;
      this.hasMore = false;
      if (tableAdvanceButton != null) {
        this.tableAdvanceButton.setEnabled(hasMore);
      }
    }

    if (table == null) {
      // Clear the table
      this.setRowCount(0);
 
    } else {
      updateData(table);
    }

  }

  // set up the callback object
  AsyncCallback<TableContentsClient> getDataCallback = new AsyncCallback<TableContentsClient>() {
    @Override
    public void onFailure(Throwable caught) {
      if (caught instanceof EntityNotFoundExceptionClient) {
        // if this happens it is PROBABLY, but not necessarily, because
        // we've deleted the table.
        // TODO ensure the correct exception makes it here
        ((OdkTablesViewTableSubTab) AggregateUI.getUI().getSubTab(SubTabs.VIEWTABLE))
            .setTabToDisplayZero();
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
      refreshCursor = tcc.websafeRefetchCursor;
      resumeCursor = tcc.websafeResumeCursor;
      hasMore = tcc.hasMore;
      if (tableAdvanceButton != null) {
        tableAdvanceButton.setEnabled(hasMore);
      }

      rows = new ArrayList<RowClientWrapper>();
      for (RowClient row: tcc.rows) {
        rows.add(new RowClientWrapper(row));
      }
   
      setColumnHeadings();
      setRows(rows);
    }
  };

  public void nextPage() {
    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES) && hasMore) {
      SecureGWT.getServerDataService().getTableContents(currentTable.getTableId(), resumeCursor,
          AggregateUI.getUI().getUserInfo().getOfficeId(), getDataCallback);
    }
  }

  public void updateData(TableEntryClient table) {
    // TODO: paginate this
    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
      SecureGWT.getServerDataService().getTableContents(table.getTableId(), refreshCursor,
          AggregateUI.getUI().getUserInfo().getOfficeId(), getDataCallback);
    }
  }

  /**
   * This is the method that actually updates the column headings. It is its own
   * method so that it can be called cleanly in the updateTableData method. If
   * the code is AFTER the call to SecureGWT, as it was at first, you can get
   * null pointer exceptions, as the async callback may have not returned.
   */
  private void setColumnHeadings() {
    // Remove all rows
    this.setRowCount(0);


    // If there are no user-defined columns display the message.
    // Otherwise set the headings.
    if (rows.isEmpty()) {
      this.setEmptyTableWidget(new HTMLPanel(NO_ROWS_MESSAGE));

    } else {
      // set the delete column

      // setText(0, 0, ODKDefaultColumnNames.DELETE_ROW_HEADING);

      // make the headings

      for (String columnName : rows.get(0).getSortedColumnNameList()) {

        // Create address column.
        OdkTablesTextColumn newColumn = new OdkTablesTextColumn(columnName);

        // Add the columns.
        this.addColumn(newColumn, columnName);
      }

    }

  }

  /*
   * This will set the row values in the listbox.
   */
  private void setRows(List<RowClientWrapper> rows) {

    // if there are no columns, then we only want to display the no data
    // message.
    if (columnNames.size() == 0) {
      this.setEmptyTableWidget(new HTMLPanel(NO_DATA_MESSAGE));

      return;
      // otherwise check if there are no rows.
    } else if (rows.size() == 0) {
      this.setEmptyTableWidget(new HTMLPanel(NO_ROWS_MESSAGE));

    } else { // there are rows--display them.

      this.setRowCount(rows.size(), true);

      // Push the data into the widget.
      this.setRowData(0, rows);

    }
  }

  /**
   * Returns the view this table is currently displaying.
   */
  public TableEntryClient getCurrentTable() {
    return currentTable;
  }

}

package org.opendatakit.aggregate.client.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * Sort column when heading is clicked.
 */
public class ColumnSortHandler implements ClickHandler {
  final HasSortColumn table;
  final SortableColumnLabel sortableColumnLabel;

  
  public ColumnSortHandler(HasSortColumn table, SortableColumnLabel sortableColumnLabel) {
    this.table = table; 
    this.sortableColumnLabel = sortableColumnLabel;
  }
  
  @Override
  public void onClick(ClickEvent event) {
    int numColumns =  table.getNumberColumns();
    // Not typesafe, technically this doesn't have to be a FlexTable.  But we know it is.
    FlexTable flexTable =  (FlexTable)table;

    for (int i = 0; i < numColumns; i++) {
      SortableColumnLabel columnLabel = (SortableColumnLabel) flexTable.getWidget(0, i);
      columnLabel.removeStyleName("sortColumn");
      columnLabel.removeStyleName("sortColumnAscending");
      columnLabel.removeStyleName("sortColumnDescending");
      columnLabel.setActiveColumn(false);
    }
    sortableColumnLabel.setActiveColumn(true);
    sortableColumnLabel.setAscending(!sortableColumnLabel.isAscending());
    if (sortableColumnLabel.isAscending()) {
      sortableColumnLabel.addStyleName("sortColumnAscending");

    } else {
      sortableColumnLabel.addStyleName("sortColumnDescending");

    }
    table.setAscending(sortableColumnLabel.isActiveColumn());
    table.setSortColumn(sortableColumnLabel.getDbColumnName());
    table.updateDisplay();
  } 
}
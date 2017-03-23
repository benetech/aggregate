package org.opendatakit.aggregate.client.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * Sort column when heading is clicked.
 */
public class ColumnSortHandler implements ClickHandler {
  final SortableFlexTable table;
  final SortableColumnLabel sortableColumnLabel;

  
  public ColumnSortHandler(SortableFlexTable table, SortableColumnLabel sortableColumnLabel) {
    this.table = table; 
    this.sortableColumnLabel = sortableColumnLabel;
  }
  
  @Override
  public void onClick(ClickEvent event) {
    int numColumns =  table.getNumberColumns();

    for (int i = 0; i < numColumns; i++) {
      SortableColumnLabel columnLabel = (SortableColumnLabel) table.getWidget(0, i);
      if (columnLabel != null) {
        columnLabel.removeStyleName("sortColumn");
        columnLabel.removeStyleName("sortColumnAscending");
        columnLabel.removeStyleName("sortColumnDescending");
        columnLabel.setActiveColumn(false);
      }
    }
    sortableColumnLabel.setActiveColumn(true);
    sortableColumnLabel.addStyleName("sortColumn");

    if (table.getSortColumn().equals(sortableColumnLabel.getDbColumnName())) {
      sortableColumnLabel.setAscending(!table.isAscending());
    }
    if (sortableColumnLabel.isAscending()) {
      sortableColumnLabel.addStyleName("sortColumnAscending");

    } else {
      sortableColumnLabel.addStyleName("sortColumnDescending");
    }
    table.setAscending(sortableColumnLabel.isAscending());
    table.setSortColumn(sortableColumnLabel.getDbColumnName());
    table.updateDisplay();
  } 
}
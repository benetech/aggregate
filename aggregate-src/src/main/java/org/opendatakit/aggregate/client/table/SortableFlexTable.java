package org.opendatakit.aggregate.client.table;

import com.google.gwt.user.client.ui.FlexTable;

public abstract class SortableFlexTable extends FlexTable implements HasSortColumn {
  // Seriously, we can't derive this from the Flextable? Awesome.
  private int numColumns;

  // Column currently being sorted on
  private String sortColumn;
  private boolean ascending = true;
  private boolean updateColumns = true;

  /* Implementing HasSortColumn */
  @Override
  public void setSortColumn(String column) {
    this.sortColumn = column;
  }

  @Override
  public String getSortColumn() {
    return this.sortColumn;
  }

  @Override
  public int getNumberColumns() {
    return this.numColumns;
  }
  
  @Override
  public void setNumberColumns(int numColumns) {
    this.numColumns = numColumns;
  }


  @Override
  public boolean isAscending() {
    return ascending;
  }

  @Override
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }
  
  @Override
  public boolean isUpdateColumns() {
    return updateColumns;
  }

  @Override
  public void setUpdateColumns(boolean flag) {
    this.updateColumns = flag;    
  }
  
  

}

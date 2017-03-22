package org.opendatakit.aggregate.client.table;

import com.google.gwt.user.client.ui.Label;

public class SortableColumnLabel extends Label {

  boolean activeColumn;
  boolean ascending;
  String dbColumnName;


  public SortableColumnLabel(String text, String dbColumnName) {
    super(text);
    // The first click will set this to true.
    this.ascending = false;
    this.activeColumn = false;
    this.dbColumnName = dbColumnName;

  }

  /**
   * @return the activeColumn
   */
  public boolean isActiveColumn() {
    return activeColumn;
  }

  /**
   * @param activeColumn
   *          the activeColumn to set
   */
  public void setActiveColumn(boolean activeColumn) {
    this.activeColumn = activeColumn;
  }

  /**
   * @return the ascending
   */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * @param ascending
   *          the ascending to set
   */
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  /**
   * @return the dbColumnName
   */
  public String getDbColumnName() {
    return dbColumnName;
  }

  /**
   * @param dbColumnName the dbColumnName to set
   */
  public void setDbColumnName(String dbColumnName) {
    this.dbColumnName = dbColumnName;
  }

  
  
}

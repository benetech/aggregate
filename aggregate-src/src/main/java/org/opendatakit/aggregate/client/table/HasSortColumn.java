package org.opendatakit.aggregate.client.table;

public interface HasSortColumn {

  /**
   * Set the column to be sorted on. Use the name in the database.
   * 
   * @param column to be sorted on
   */
  void setSortColumn(String column);

  /**
   * Get the column to be sorted on. Use the name in the database.
   * 
   * @return column to be sorted on
   */
  String getSortColumn();

  /**
   * Refresh the display (after updating which column to sort on.)
   */
  void updateDisplay();

  /**
   * Get the number of columns, because FlexTable does not supply this.
   * 
   * @return number of columns
   */
  int getNumberColumns();
  
  /**
   * Set the number of columns.
   * 
   * @param numColumns number of columns
   */
  void setNumberColumns(int numColumns);

  /**
   * Returns whether sort is ascending.
   * 
   * @return true if sort is ascending
   */
  boolean isAscending();

  /**
   * Sets whether sort is ascending.
   * 
   * @param ascending set to true if sort is ascending
   */
  void setAscending(boolean ascending);

}

package org.opendatakit.aggregate.client.table;

public enum InstanceFileInfoColumnType {

  DELETE(null, "Delete", 0), 
  INSTANCE_ID("_URI", "Row ID", 1), 
  FILENAME("UNROOTED_URI_PATH","Filename", 2), 
  CONTENT_LENGTH("CONTENT_LENGTH", "Size", 3), 
  CONTENT_TYPE("CONTENT_TYPE", "Content Type", 4),
  DOWNLOAD(null, "Download", 5);

  private String dbColumn;
  private String displayText;
  private int displayColumn;

  InstanceFileInfoColumnType(String dbColumn, String displayText, int displayColumn) {
    this.dbColumn = dbColumn;
    this.displayText = displayText;
    this.displayColumn = displayColumn;
  }

  /**
   * @return the dbColumn
   */
  public String getDbColumn() {
    return dbColumn;
  }

  /**
   * @return the displayText
   */
  public String getDisplayText() {
    return displayText;
  }

  /**
   * @return the displayColumn
   */
  public int getDisplayColumn() {
    return displayColumn;
  }

}

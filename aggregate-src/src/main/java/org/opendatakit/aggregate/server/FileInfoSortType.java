package org.opendatakit.aggregate.server;

import java.util.Comparator;

import org.opendatakit.aggregate.client.odktables.FileSummaryClient;

public enum FileInfoSortType {

  INSTANCE_ID("_URI", new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg0.getInstanceId().compareTo(arg1.getInstanceId());
    }
  }, new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg1.getInstanceId().compareTo(arg0.getInstanceId());
    }
  }), FILENAME("UNROOTED_URI_PATH", new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg0.getFilename().compareTo(arg1.getFilename());
    }
  }, new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg1.getFilename().compareTo(arg0.getFilename());
    }
  }), CONTENT_LENGTH("CONTENT_LENGTH", new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg0.getContentLength().compareTo(arg1.getContentLength());
    }
  }, new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg1.getContentLength().compareTo(arg0.getContentLength());
    }
  }), CONTENT_TYPE("CONTENT_TYPE", new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg0.getContentType().compareTo(arg1.getContentType());
    }
  }, new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg1.getContentType().compareTo(arg0.getContentType());
    }
  }), ODK_CLIENT_VERSION("_ODK_CLIENT_VERSION", new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg0.getOdkClientVersion().compareTo(arg1.getOdkClientVersion());
    }
  }, new Comparator<FileSummaryClient>() {
    @Override
    public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
      return arg1.getOdkClientVersion().compareTo(arg0.getOdkClientVersion());
    }
  });

  private Comparator<FileSummaryClient> ascendingSort;
  private Comparator<FileSummaryClient> descendingSort;
  private String dbColumn;

  FileInfoSortType(String dbColumn, Comparator<FileSummaryClient> ascendingSort,
      Comparator<FileSummaryClient> descendingSort) {
    this.dbColumn = dbColumn;
    this.ascendingSort = ascendingSort;
    this.descendingSort = descendingSort;
  }

  /**
   * @return the ascendingSort
   */
  public Comparator<FileSummaryClient> getAscendingSort() {
    return ascendingSort;
  }

  /**
   * @return the descendingSort
   */
  public Comparator<FileSummaryClient> getDescendingSort() {
    return descendingSort;
  }

  /**
   * @return the dbColumn
   */
  public String getDbColumn() {
    return dbColumn;
  }

  public static FileInfoSortType fromDbColumn(String dbColumn) {
    if (dbColumn == null) {
      return null;
    }
    for (FileInfoSortType value : FileInfoSortType.values()) {
      if (dbColumn.equals(value.getDbColumn())) {
        return value;
      }
    }
    return null;
  }

}

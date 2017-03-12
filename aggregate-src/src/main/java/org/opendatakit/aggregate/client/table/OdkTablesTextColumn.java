package org.opendatakit.aggregate.client.table;

import com.google.gwt.user.cellview.client.TextColumn;

public class OdkTablesTextColumn extends TextColumn<RowClientWrapper> {

  String columnKey;
  
  private OdkTablesTextColumn() {
    super();
  }

  public OdkTablesTextColumn(String key) {
    super();
    this.columnKey = key;
  }

  @Override
  public String getValue(RowClientWrapper row) {
    return row.getValueMapWithDefaults().get(columnKey);
  }

}

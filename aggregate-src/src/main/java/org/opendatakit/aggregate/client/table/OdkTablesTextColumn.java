package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.odktables.RowClient;

import com.google.gwt.user.cellview.client.TextColumn;

public class OdkTablesTextColumn extends TextColumn<RowClient> {

  String columnKey;
  
  private OdkTablesTextColumn() {
    super();
  }

  public OdkTablesTextColumn(String key) {
    super();
    this.columnKey = key;
  }

  @Override
  public String getValue(RowClient row) {
    return row.getValueMapWithDefaults().get(columnKey);
  }

}

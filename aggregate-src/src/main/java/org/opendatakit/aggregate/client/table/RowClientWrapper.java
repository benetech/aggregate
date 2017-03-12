package org.opendatakit.aggregate.client.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.util.CollectionUtils;
import org.opendatakit.aggregate.constants.common.ODKDefaultColumnNames;

public class RowClientWrapper extends RowClient{
  private static final long serialVersionUID = 629805895995062187L;

  // Lazily-initialized fields that rearrange the data for convenience

  private transient List<String> sortedValueList;
  
  private transient Map<String,String> valueMapWithDefaults;
  
  private transient List<String> sortedColumnNameList;
  

  public RowClientWrapper(RowClient rowClient) {
    super(rowClient);
  }

  /**
   * Get the values, including the ODK default columns.
   * Transient convenience field not included in ODK Tables API row definition.
   * @return map of values including the default ODK columns.
   */
  public Map<String,String> getValueMapWithDefaults() {
    if (valueMapWithDefaults == null) {
      valueMapWithDefaults = new HashMap<String,String>();
      valueMapWithDefaults.putAll(this.getValues());
      valueMapWithDefaults.put(ODKDefaultColumnNames.SAVEPOINT_TYPE, this.getSavepointType());
      valueMapWithDefaults.put(ODKDefaultColumnNames.FORM_ID, this.getFormId());
      valueMapWithDefaults.put(ODKDefaultColumnNames.LOCALE, this.getLocale());
      valueMapWithDefaults.put(ODKDefaultColumnNames.SAVEPOINT_TIMESTAMP, this.getSavepointTimestampIso8601Date());
      valueMapWithDefaults.put(ODKDefaultColumnNames.SAVEPOINT_CREATOR, this.getSavepointCreator());
      valueMapWithDefaults.put(ODKDefaultColumnNames.ROW_ID,this.getRowId());
      valueMapWithDefaults.put(ODKDefaultColumnNames.ROW_ETAG, this.getRowETag());
      valueMapWithDefaults.put(ODKDefaultColumnNames.FILTER_TYPE, this.getRowFilterScope().getType().name());
      valueMapWithDefaults.put(ODKDefaultColumnNames.FILTER_VALUE, this.getRowFilterScope().getValue());
      valueMapWithDefaults.put(ODKDefaultColumnNames.LAST_UPDATE_USER, this.getLastUpdateUser());
      valueMapWithDefaults.put(ODKDefaultColumnNames.CREATED_BY_USER, this.getCreateUser());
      valueMapWithDefaults.put(ODKDefaultColumnNames.DATA_ETAG_AT_MODIFICATION, this.getDataETagAtModification());
    }
    return valueMapWithDefaults;
  }

  /**
   * Return the columns in sorted order.
   * User column name in alphabetical order followed by the ODK default column names.
   * Transient convenience field not included in ODK Tables API row definition.
   * @return Sorted list of columns.
   */
  public List<String> getSortedColumnNameList() {
    if (sortedColumnNameList == null) {
      sortedColumnNameList = new ArrayList<String>();
      for (String columnName : CollectionUtils.asSortedList(this.getValues().keySet())) {
        sortedColumnNameList.add(columnName);
      }
      for (String defaultColumn : ODKDefaultColumnNames.ORDERED_LIST) {
        sortedColumnNameList.add(defaultColumn);
      }
    }
    return sortedColumnNameList;
  }

  /**
   * Get row values sorted in the same order as the getColumnNameList.
   * Transient convenience field not included in ODK Tables API row definition.
   * @return
   */
  public List<String> getSortedValueList() {
    if (sortedValueList == null) {
      sortedValueList = new ArrayList<String>();

      for (String columnName : this.getSortedColumnNameList()) {
        sortedValueList.add(this.getValueMapWithDefaults().get(columnName));
      }
    }
    return sortedValueList;
  }
}

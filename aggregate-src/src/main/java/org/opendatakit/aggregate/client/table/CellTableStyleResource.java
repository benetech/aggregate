package org.opendatakit.aggregate.client.table;

import com.google.gwt.user.cellview.client.CellTable;

/**
 * Override CellTable CSS.
 * @author Caden Howell
 *
 */
public interface CellTableStyleResource  extends CellTable.Resources{
  @Override
  @Source(value = "ODKCellTable.css")
  CellTable.Style cellTableStyle();
}

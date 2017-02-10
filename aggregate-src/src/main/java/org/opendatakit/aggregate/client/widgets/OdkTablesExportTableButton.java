package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.opendatakit.aggregate.client.popups.ExportTablePopup;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;

public class OdkTablesExportTableButton extends AggregateButton implements
        ClickHandler {

        private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Export";
        private static final String TOOLTIP_TXT = "Export Table";
        private static final String HELP_BALLOON_TXT = "Export Selected Data Table.";

        /**
         * This is the parent table that contains the elements which this button
         * is responsible for exporting. This is so you can call refresh on it
         * without needing to refresh the whole page.
         */
        private OdkTablesTableList parentTable;

        private String tableId;

	  public OdkTablesExportTableButton(OdkTablesTableList parentTable,
            String tableId) {
        super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
        this.parentTable = parentTable;
        this.tableId = tableId;
    }

        @Override
        public void onClick(ClickEvent event) {
        super.onClick(event);
        ExportTablePopup popup = new ExportTablePopup(this.tableId);
        popup.setPopupPositionAndShow(popup.getPositionCallBack());
    }

}

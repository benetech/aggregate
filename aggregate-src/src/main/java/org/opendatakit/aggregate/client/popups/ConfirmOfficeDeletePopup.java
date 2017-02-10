package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.RegionalOffice;
import org.opendatakit.aggregate.client.permissions.AccessConfigurationSheet;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * Created by user on 17.01.17.
 */
public final class ConfirmOfficeDeletePopup extends AbstractPopupBase {
    private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Delete User";
    private static final String TOOLTIP_TXT = "Remove this user";
    private static final String HELP_BALLOON_TXT = "Remove this user from the server.";


    private final AccessConfigurationSheet accessSheet;
    private final RegionalOffice office;

    public ConfirmOfficeDeletePopup(RegionalOffice officeToDelete, AccessConfigurationSheet sheet) {
        super();
        this.accessSheet = sheet;
        this.office = officeToDelete;

        AggregateButton deleteOfficeButton = new AggregateButton("<img src=\"images/green_right_arrow.png\" /> Delete Office", "Remove this Office", "Remove this office from the server.");
        deleteOfficeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                accessSheet.deleteOffice(office);
                hide();
            }
        });
        FlexTable layout = new FlexTable();

        HTML message;
        if (sheet.isUiOutOfSyncWithServer()) {
            message = new HTML(
                    "Unsaved changes exist.<br/>"
                            + "<p>Proceeding will save all pending changes and<br/>permanently delete office <b>"
                            + officeToDelete.getOfficeID()
                            + "</b> on the server.</p>"
                            + "<p>Do you wish to apply all pending changes and <br/>permanently delete this office?</p>");
        } else {
            message = new HTML("<p>Proceeding will permanently delete office <b>"
                    + officeToDelete.getOfficeID()
                    + "</b> on the server.</p><p>Do you wish to permanently delete this office?</p>");
        }
        layout.setWidget(0, 0, message);
        layout.setWidget(2, 0, deleteOfficeButton);
        layout.setWidget(2, 1, new ClosePopupButton(this));

        setWidget(layout);
}
}
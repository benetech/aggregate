package org.opendatakit.aggregate.constants.common;

import java.io.Serializable;

/**
 * Enum of what type of file can be exported
 * CSV or JSONFILE
 */
public enum ExportTableType implements Serializable {
    CSV("CSV file"),
    JSONFILE("JSON file");

    private String displayText;

    private ExportTableType() {
        // GWT
    }

    private ExportTableType(String display) {
        displayText = display;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String toString() {
        return displayText;
    }

}

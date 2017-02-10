package org.opendatakit.aggregate.client;

/**
 * Created by user on 04.01.17.
 */
import java.io.Serializable;

public class RegionalOffice implements Serializable{

    private static final long serialVersionUID = 762805275766667474L;

    private String name;

    private String officeID;

    private String URI;

    private boolean removed;

    public RegionalOffice(){
    }

    public RegionalOffice(String name, String officeID){
    this.name = name;
    this.officeID = officeID;
    }

    public RegionalOffice(String URI, String name, String officeID){
        this.URI = URI;
        this.name = name;
        this.officeID = officeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOfficeID() {
        return officeID;
    }

    public void setOfficeID(String officeID) {
        this.officeID = officeID;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}

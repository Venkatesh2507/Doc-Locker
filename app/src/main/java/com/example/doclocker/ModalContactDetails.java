package com.example.doclocker;

import java.io.Serializable;

public class ModalContactDetails implements Serializable {
    String contactName,contactNumber;

    public ModalContactDetails(String contactName, String contactNumber) {
        this.contactName = contactName;
        this.contactNumber = contactNumber;
    }
    public ModalContactDetails(){}

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}

package com.pandamedia.beans;

/**
 * Holds phone number value
 * @author Hau Gilles Che
 */
public class PhoneNumberBean {
    private String phoneNumber;

    public PhoneNumberBean(){    
    }
    
    public PhoneNumberBean(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return phoneNumber;
    }    
}

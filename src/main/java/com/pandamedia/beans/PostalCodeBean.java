package com.pandamedia.beans;

/**
 * Holds the information of a postal code
 * @author Hau Gilles Che
 */
public class PostalCodeBean {
    private String code;
    
    public PostalCodeBean(){
    }
    
    public PostalCodeBean(String code){
        this.code=code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
    
    
}

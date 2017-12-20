package com.pandamedia.beans;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import persistence.entities.Province;
import persistence.entities.ShopUser;


/**
 * This bean provides an easy interface for an xhtml to fill out a user object.
 * 
 * @author Hau Gilles Che
 */
@Named
@RequestScoped
public class UserBean implements Serializable {
    
    // User fields. 
    private ShopUser user;
    private String password;
    private PostalCodeBean postalCode;
    private PhoneNumberBean homePhone;
    private PhoneNumberBean cellPhone;

    public ShopUser getShopUser() {
        if(user == null)
            user = new ShopUser();
        
        return user;
    }

    public void setShopUser(ShopUser user) {
        this.user = user;
    }
    
    public String getPassword() {
        if(password == null)
            password = "";
        return password;
    }

    public void setPassword(String confirmPwd) {
        this.password = confirmPwd;
    }

    public PostalCodeBean getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(PostalCodeBean postalCode) {
        this.postalCode = postalCode;
    }

    public PhoneNumberBean getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(PhoneNumberBean homePhone) {
        this.homePhone = homePhone;
    }

    public PhoneNumberBean getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(PhoneNumberBean cellPhone) {
        this.cellPhone = cellPhone;
    }
}

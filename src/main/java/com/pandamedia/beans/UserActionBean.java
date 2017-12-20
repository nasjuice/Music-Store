package com.pandamedia.beans;

import com.pandamedia.utilities.PasswordHelper;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import jodd.mail.EmailAddress;
import persistence.controllers.ProvinceJpaController;
import persistence.controllers.ShopUserJpaController;
import persistence.controllers.UserActionController;
import persistence.entities.Province;
import persistence.entities.ShopUser;

/**
 * Responsible for user connection interaction (login, logout,registration)
 *
 * @author Hau Gilles Che
 * @version 1.0.01
 */
@Named("userAction")
@SessionScoped
public class UserActionBean implements Serializable {

    @Inject
    private UserActionController userActionController;

    @Inject
    private ShopUserJpaController userController;

    @Inject
    private UserBean userBean;

    @Inject
    private ProvinceJpaController provinceController;

    private final PasswordHelper pwdHelper = new PasswordHelper();

    // Needed variables for login and registration
    private ShopUser currUser;
    private List<Province> provinces;
    private Province province;
    // variable to hold previous page, used to navigate to after redirection
    private UIViewRoot prevPage;

    @PostConstruct
    public void init() {
        provinces = provinceController.findProvinceEntities();
    }

    public ShopUser getcurrUser() {
        return currUser;
    }

    public List<Province> getProvinces() {
        return provinces;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    /**
     * Responsible for creating a new user records
     *
     * @param province Id of the province chosen by the user.
     */
    public String register(String province) {
        String pageRedirect = null;
        
        ShopUser user = userBean.getShopUser();
        setFields(user);
        int provinceId = Integer.parseInt(province);
        Province prov = provinceController.findProvince(provinceId);
        user.setProvinceId(prov);
        try {
            userController.create(user);
            currUser = user;
            if (prevPage != null) {
                FacesContext.getCurrentInstance().setViewRoot(prevPage);
                FacesContext.getCurrentInstance().renderResponse();
            } else {
                pageRedirect = "main";
            }

        } catch (IOException ioe) {
            Logger.getLogger(UserActionBean.class.getName())
                    .log(Level.WARNING, "Error when redirecting: {0}",
                            ioe.getMessage());

        } catch (Exception ex) {
            FacesMessage msg = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "duplicateEmail", null);
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(
                    "registrationForm:emailInput", msg);
        }
        return pageRedirect;
    }

    /**
     * Responsible for login in a user.
     * 
     * @author Hau Gilles Che
     * @author Erika Bourque
     */
    public String login() throws IOException {
        String pageRedirect = null;
        currUser = userBean.getShopUser();
        ShopUser userRecord = userActionController.findUserByEmail(
                currUser.getEmail());
        ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
        external.getSessionMap().put("user", currUser);

        if (userRecord == null) {
            System.out.println("USER IS NULL");
            FacesMessage msg = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "duplicateEmail", null);
            FacesContext.getCurrentInstance().addMessage("loginForm", msg);
        } else {
            byte[] hashRecord = userRecord.getHashedPw();
            byte[] loginPwdHash = pwdHelper.hash(userBean.getPassword(),
                    userRecord.getSalt());

            if (!Arrays.equals(hashRecord, loginPwdHash)) {
                System.out.println("BAD PASSWORD");
                FacesMessage msg = com.pandamedia.utilities.Messages.getMessage(
                        "bundles.messages", "invalidEmailOrPwd", null);
                FacesContext.getCurrentInstance().addMessage("loginForm", msg);
                currUser = null;
            } else {
                currUser = userRecord;
                external.getSessionMap().put("user", userRecord);
                // Making user to redirect to manager side if it is a manager
                // check to see if the user was being redirected from another
                // page. If they have  not, they should be redirected to the mainpage
                if (currUser.getIsManager() == 1) {
                    pageRedirect = "manindex";
                } else if (prevPage != null) {
                    FacesContext.getCurrentInstance().setViewRoot(prevPage);
                    FacesContext.getCurrentInstance().renderResponse();
                } else {
                    pageRedirect = "main";
                }
            }
        }
        return pageRedirect;
    }

    /**
     * Responsible for login out users.
     *
     * @author Hau Gilles Che
     * @author Erika Bourque
     */
    public String logout() {
        String pageRedirect = null;
        //for security purposes, if an admin logout, redirects to index
        if (currUser.getIsManager() == 1) {
            currUser = null;
            pageRedirect = "main";
        }
        UIViewRoot currentPage = FacesContext.getCurrentInstance().getViewRoot();
        System.out.println(currentPage.getViewId());
        if (currentPage.getViewId().startsWith("/clientsecure/")) {
            pageRedirect = "main";

        }
        currUser = null;
        // destroy user object from session map.
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", null);
        return pageRedirect;
    }

    /**
     * Checks if a user is logged in.
     *
     * @return true if a user is logged in, false otherwise.
     */
    public boolean isLogin() {
        return currUser != null;
    }

    /**
     *
     * @return the current logged in user.
     */
    public ShopUser getCurrUser() {
        return currUser;
    }

    /**
     * Sets the remaining fields that cannot be directly set by the user through
     * the form.
     *
     * @param user
     */
    private void setFields(ShopUser user) {
        String salt = pwdHelper.getSalt();

        byte[] hashedPwd = pwdHelper.hash(userBean.getPassword(), salt);

        user.setSalt(salt);
        user.setHashedPw(hashedPwd);

        user.setPostalCode(userBean.getPostalCode().toString());
        user.setHomePhone(userBean.getHomePhone().toString());

        if (userBean.getCellPhone() != null) {
            user.setCellPhone(userBean.getCellPhone().toString());
        }
    }

    /**
     * Validates the email address to make sure it is properly formatted.
     *
     * @param fc
     * @param c
     * @param value
     */
    public void validateEmail(FacesContext fc, UIComponent c, Object value) {
        EmailAddress email = new EmailAddress((String) value);

        if (!email.isValid()) {
            FacesMessage msg = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "badEmailFormat", null);
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

    /**
     * Sets the previous page variable to the page that the user is currently
     * on. The variable is used to hold a view to where the user used to be
     * before they were redirected to the login page. Returns the string for the
     * login nav rule.
     *
     * @author Erika Bourque
     */
    public String setPrevPageLogin() {
        prevPage = FacesContext.getCurrentInstance().getViewRoot();
        return "login";
    }

    /**
     * Sets the previous page variable to the page that the user is currently
     * on. The variable is used to hold a view to where the user used to be
     * before they were redirected to the login page. Returns the string for the
     * registration nav rule.
     *
     * @author Erika Bourque
     */
    public String setPrevPageRegister() {
        prevPage = FacesContext.getCurrentInstance().getViewRoot();
        return "register";
    }

    /**
     * Redirects the user to the login page if they're not logged in. It sets
     * the users previous page for when the user is done with logging in /
     * registering, they can easily be navigated to the page they were
     * previously on.
     *
     * @author Evan Glicakis
     * @param s the string representation of the page that the user is
     * navigating to.
     * @return
     */
    public String redirectUser(String s) {
        if (this.isLogin()) {
            return s;
        } else {
            //Sets the page where the user was previously on.
            System.out.println("prev page");
            this.prevPage = FacesContext.getCurrentInstance().getViewRoot();
            return "login";
        }
    }

    public void setUser(ShopUser user) {
        this.currUser = user;
    }

}

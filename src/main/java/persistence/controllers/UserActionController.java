package persistence.controllers;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import persistence.entities.ShopUser;

/**
 * Holds custom non CRUD methods to access user records.
 *
 * @author Hau Gilles Che
 */
@Named
@RequestScoped
public class UserActionController {
    @PersistenceContext
    private EntityManager em;

    public UserActionController() {
    }

    public ShopUser findUserByEmail(String email) {
        if(email == null)
            return null;
        Query query = em.createNamedQuery("ShopUser.findByEmail");
        query.setParameter("email", email);
        ShopUser user = null;
        try {
            user = (ShopUser) query.getSingleResult();
        } catch (EntityNotFoundException | NonUniqueResultException
                | NoResultException ex) {
            FacesMessage msg = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "duplicateEmail", null);
            FacesContext.getCurrentInstance().addMessage("loginForm", msg);
        }

        return user;
    }
}


package com.pandamedia.beans;

import java.io.IOException;
import persistence.controllers.ShopUserJpaController;
import persistence.entities.Invoice;
import persistence.entities.ShopUser;
import persistence.entities.Survey;
import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import persistence.entities.Invoice_;
import persistence.entities.ShopUser_;


/**
 * This class will be used as the user's backing bean. It can update and query 
 * users.
 * @author Evan Glicakis, Naasir Jusab
 */
@Named("userBacking")
@SessionScoped
public class ShopUserManagerBean implements Serializable{
    @Inject
    private ShopUserJpaController userController;
    private ShopUser user;
    private List<ShopUser> users;
    private List<ShopUser> filteredUsers;
    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method will initialize a list of users that will be used by the 
     * data table. PostConstruct is used in methods that need to be executed after 
     * dependency injection is done to perform any initialization. In this case,
     * I need the list of users after userController has been injected.
     */
    @PostConstruct
    public void init()
    {
        this.users = userController.findShopUserEntities();
    }
    
    /**
     * This method will return all the users in a list so it can be displayed
     * on the data table.
     * @return all users in the database
     */
    public List<ShopUser> getUsers()
    {
        return users;
    }
    
    /**
     * This method will set a list of users to make changes to the current
     * list of all users.
     * @param users all users in the database
     */
    public void setUsers(List<ShopUser> users)
    {
        this.users = users;
    }
    
    /**
     * This method will return a list of filtered users so that the manager
     * can make searches on users.
     * @return list of filteredUsers
     */
    public List<ShopUser> getFilteredUsers()
    {
        return filteredUsers;
    }
    
    /**
     * This method will set a list of filtered users to change the current
     * list of filtered users.
     * @param filteredUsers list of filtered users
     */
    public void setFilteredUsers(List<ShopUser> filteredUsers)
    {
        this.filteredUsers = filteredUsers;
    }
   
    /**
     * This method will return an user if it exists already. Otherwise, it will
     * return a new user.
     * @return user object
     */
    public ShopUser getUser(){
        if(user == null){
            user = new ShopUser();
        }
        return user;
    }
    
    /**
     * This method will return all the users in the database so it can be
     * displayed on the data table.
     * @return list of users
     */    
    public List<ShopUser> getAll()
    {
        return userController.findShopUserEntities();
    }
    
    /**
     * This method will set the user so that when the editClients.xhtml loads.
     * The fields of the page will have values already. All the manager has to  
     * do is change the values. The id will make sure that the right user is
     * being edited and the return type will display the edit page for the 
     * user.
     * @param id of the user that will be edited
     * @return string that represents the page where the registration info of 
     * an user can be edited
     */
    public String loadEditForClients(Integer id)
    {
        this.user = userController.findShopUser(id);        
        return "maneditclient";
    }
    
    /**
     * This method will be called to edit an user. At the end, the user is set
     * to null so that it does not stay in session scoped and the filtered 
     * users are regenerated.  
     * @return string that is the main page for clients
     */
    public String edit() 
    {
        try
        {
            userController.edit(user);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        
        this.user = null;
        this.filteredUsers = userController.findShopUserEntities();        
        return "manclients";
    }
    
    /**
     * This method is used to return back to the clients home page. Also, the 
     * user is set to null so that it does not stay in session scoped and the
     * filtered users are regenerated. 
     * @return clients home page
     */
    public String back()
    {
        this.user = null;
        this.filteredUsers = userController.findShopUserEntities();        
        return "manclients";
    }
    
    /**
     * This method is used to get the total purchases of a client. The
     * number formatter is used to make the purchases only two digits after the 
     * decimal point and if there are no purchases made by the client then 0 is
     * returned.
     * @param id of the client whose purchases will be displayed
     * @return string that is the purchases of the client
     */
    public String getClientTotalPurchase(Integer id) 
    {
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Double> query = cb.createQuery(Double.class);
        Root<ShopUser> userRoot = query.from(ShopUser.class);
        Join invoiceJoin = userRoot.join(ShopUser_.invoiceList);
        query.select(cb.sum(invoiceJoin.get(Invoice_.totalGrossValue)));
        query.groupBy(userRoot.get(ShopUser_.id));

        // Where clause
        Predicate p1 = cb.equal(userRoot.get(ShopUser_.id), id);
        Predicate p2 = cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0);
        query.where(cb.and(p1, p2));

        TypedQuery<Double> typedQuery = em.createQuery(query);
        NumberFormat formatter = new DecimalFormat("#0.00"); 
        
        if(typedQuery.getResultList().size() == 0)
            return "0.0";
        else
            return formatter.format(typedQuery.getResultList().get(0));
    }
    
    public ShopUser findUserById(Integer id)
    {
        return userController.findShopUser(id);
    }
    
    public void setShopUser(ShopUser user)
    {
        this.user = user;
    }

}
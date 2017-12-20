
package com.pandamedia.beans;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import persistence.controllers.ProvinceJpaController;
import persistence.entities.Province;

/**
 * This class will be used as the province backing bean. It is used as a means
 * of getting provinces and querying them.
 * @author Naasir Jusab
 */
@Named("provinceBacking")
@SessionScoped
public class ProvinceBackingBean implements Serializable{
    @Inject
    private ProvinceJpaController provinceController;
    private Province province;
    @PersistenceContext
    private EntityManager em;
        
    /**
     * This method will return a province if it exists already. Otherwise, it 
     * will return a new province object.
     * @return province object
     */
    public Province getProvince(){
        if(province == null){
            province = new Province();
        }
        return province;
    }
    
    /**
     * This method will return all the provinces in the database so it can be
     * displayed on the data table.
     * @return list of all the provinces
     */
    public List<Province> getAll()
    {
        return provinceController.findProvinceEntities();
    }
    
    
}

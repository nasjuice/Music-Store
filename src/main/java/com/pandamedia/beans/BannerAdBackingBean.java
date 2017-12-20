
package com.pandamedia.beans;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import persistence.controllers.AdvertisementJpaController;
import persistence.controllers.FrontPageSettingsJpaController;
import persistence.entities.Advertisement;
import persistence.entities.FrontPageSettings;

/**
 * This class will be used as the banner ad backing bean. It is used as a means
 * of getting banner ads and querying them.
 * @author Naasir Jusab
 */
@Named("bannerAdBacking")
@SessionScoped
public class BannerAdBackingBean implements Serializable {
    
    @Inject
    private AdvertisementJpaController advertisementController;
    @Inject
    private FrontPageSettingsJpaController fpsController;
    private Advertisement advertisement;
    @PersistenceContext
    private EntityManager em;
   
    
    /**
     * This method will return an ad if it exists already. Otherwise, it 
     * will return a new ad object.
     * @return ad object
     */
    public Advertisement getAdvertisement(){
        if(advertisement == null){
            advertisement = new Advertisement();
        }
        return advertisement;
    }
    
    /**
     * Finds the advertisement from its id.
     * @param id of the advertisement
     * @return advertisement object
     */
    public Advertisement findAdvertisementById(int id){
        return advertisementController.findAdvertisement(id);
    }
    
    /**
     * This method will return all the ads in the database so it can be 
     * displayed on the data table.
     * @return list of all the advertisements
     */
    public List<Advertisement> getAll()
    {
        return advertisementController.findAdvertisementEntities();
    }
    
    /**
     * This method will save the ad to the database and select it so that the 
     * manager can change the ad that is being displayed on the main page.
     * @return null should make it stay on the same page
     */
    public String save()
    {
        try
        {
            advertisementController.create(advertisement);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        
        this.advertisement = null;
        return null;
    }
    
    
    /**
     * This method will destroy the ad in the database and it sets the ad
     * object to null so that it does not stay in session scoped.
     * @param id of the ad object
     * @return null should make it stay on the same page
     */
    public String remove(Integer id)
    {
        //it is used on the front page don't change it unless you select another one
        if(fpsController.findFrontPageSettings(1).getAdAId().equals(advertisementController.findAdvertisement(id)))
            return null;
        try
        {
            advertisementController.destroy(id);
        }
        
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        this.advertisement = null;
        return null;
    }
    
    /**
     * This method will find the ad from its id and set it in order to change 
     * the ad being displayed on the main page.
     * @param id of the ad object
     * @return null should make it stay on the same page
     */
    public String select(Integer id)
    {
        advertisement = advertisementController.findAdvertisement(id);
        
        FrontPageSettings fps = fpsController.findFrontPageSettings(1);
        fps.setAdAId(advertisement);
        
        try
        {
            fpsController.edit(fps);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        
        this.advertisement = null;
        
        return null;
    }
    
    public void setAd(Advertisement ad)
    {
        this.advertisement = ad;
    }
    
    public Advertisement getAd()
    {
        return this.advertisement;
    }
    
    public String getAdPath(){
        return fpsController.findFrontPageSettings(1).getAdAId().getAdPath();
    }
    
    
}

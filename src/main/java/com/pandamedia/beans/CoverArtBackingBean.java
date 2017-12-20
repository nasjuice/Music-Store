
package com.pandamedia.beans;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import persistence.controllers.CoverArtJpaController;
import persistence.entities.CoverArt;


/**
 * This class will be used as the coverArt backing bean. It is used as a means
 * of getting cover arts and querying them.
 * @author Naasir Jusab
 */
@Named("coverArtBacking")
@SessionScoped
public class CoverArtBackingBean implements Serializable {
    @Inject
    private CoverArtJpaController coverArtController;
    private CoverArt coverArt;
    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method will return a cover art if it exists already. Otherwise, it 
     * will return a new cover art object.
     * @return cover art object
     */
    public CoverArt getCoverArt(){
        if(coverArt == null){
            coverArt = new CoverArt();
        }
        return coverArt;
    }
    
    /**
     * Finds the CoverArt from its id.
     * @param id of the cover art
     * @return cover art object
     */
    public CoverArt findCoverArtById(int id){
        return coverArtController.findCoverArt(id); 
    }
    
    /**
     * This method will return all the cover arts in the database so it can be 
     * displayed on the data table.
     * @return list of all the cover arts
     */
    public List<CoverArt> getAll()
    {
        return coverArtController.findCoverArtEntities();
    }
    
}

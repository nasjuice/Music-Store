package com.pandamedia.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import persistence.controllers.ArtistJpaController;
import persistence.entities.Artist;

/**
 *
 * @author Erika Bourque, Pierre Azelart, Naasir Jusab
 */
@Named("artistBacking")
@SessionScoped
public class ArtistBackingBean implements Serializable{
    private static final Logger LOG = Logger.getLogger("ArtistBackingBean.class");
    @Inject
    private ArtistJpaController artistController;
    private Artist artist;
    private List artists;
    @PersistenceContext
    private EntityManager em;
    
    public String artistPage(Artist a){
        this.artist = a;
        LOG.info("" + a.getId() +"\n" + a.getName() +"\n");
        return "artist";
	}

    /**
     * This method will return an artist if it exists already. Otherwise, it will
     * return a new artist object.
     * @return artist object
     */
    public Artist getArtist(){
        if(artist == null){
            artist = new Artist();
        }
        return artist;
    }
    
    /**
     * Finds the Artist from its id.
     * @param id of the artist
     * @return artist object
     */
    public Artist findArtistById(int id){
        return artistController.findArtist(id); 
    }
    
    /**
     * This method will return all the artists in the database so it can be 
     * displayed on the data table.
     * @return list of all the artists
     */
    public List<Artist> getAll()
    {
        return artistController.findArtistEntities();
    }
    
    /**
     * This method will change the current artist object.
     * @param artist new artist object
     */
    public void setArtist(Artist artist)
    {
        LOG.info("New artist id: " + artist.getId());
        this.artist = artist;
    }
    
    public List<Artist> getArtistsList(){
       return getAll();
    }
    
    public void setArtistList(ArrayList<Artist> a){
        this.artists = a;
    }
}

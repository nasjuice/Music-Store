
package com.pandamedia.beans;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import persistence.controllers.SongwriterJpaController;
import persistence.entities.Songwriter;


/**
 * This class will be used as the song writer backing bean. It is used as a means
 * of getting song writers and querying them.
 * @author Naasir Jusab
 */
@Named("songWriterBacking")
@SessionScoped
public class SongWriterBackingBean implements Serializable{
    @Inject
    private SongwriterJpaController songWriterController;
    private Songwriter songWriter;
    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method will return a songWriter if it exists already. Otherwise, it 
     * will return a new songWriter object.
     * @return songWriter object
     */
    public Songwriter getSongwriter(){
        if(songWriter == null){
            songWriter = new Songwriter();
        }
        return songWriter;
    }
    
    /**
     * This method will return all the songWriters in the database so it can be 
     * displayed on the data table.
     * @return list of all the songWriters
     */
    public List<Songwriter> getAll()
    {
        return songWriterController.findSongwriterEntities();
    }
    
    

}

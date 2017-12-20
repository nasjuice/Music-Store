
package com.pandamedia.beans;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import persistence.controllers.GenreJpaController;
import persistence.entities.Genre;

/**
 * This class will be used as the genre backing bean. It is used as a means
 * of getting genres and querying them.
 * @author Naasir Jusab, Evan Glicakis
 */
@Named("genreBacking")
@SessionScoped
public class GenreBackingBean implements Serializable {
    @Inject
    private GenreJpaController genreController;
    private Genre genre;
    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method will return a genre if it exists already. Otherwise, it will
     * return a new genre object.
     * @return genre object
     */    
    public Genre getGenre(){
        if(genre == null){
            genre = new Genre();
        }
        return genre;
    }
    /**
     * Used to get a list of strings of all the genre names in the database
     * used by the browse albums page to have a list of all the available genres.
     * @author Evan G.
     * @return 
     */
    public List<String> getAllGenresNames(){
        String q = "SELECT g.name FROM Genre g";
        TypedQuery query = em.createQuery(q, Genre.class);
        return query.getResultList();
    }
    
    /**
     * This method will return all the genres in the database so it can be 
     * displayed on the data table.
     * @return list of all the genres
     */
    public List<Genre> getAll()
    {
        return genreController.findGenreEntities();
    }
    
    
}

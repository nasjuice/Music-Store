
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
import persistence.controllers.FrontPageSettingsJpaController;
import persistence.controllers.NewsfeedJpaController;
import persistence.entities.FrontPageSettings;
import persistence.entities.Newsfeed;

/**
 * This class will be used as the news feed backing bean. It is used as a means
 * of getting news feeds and querying them.
 * @author Naasir Jusab
 */
@Named("rssFeedBacking")
@SessionScoped
public class RSSFeedBackingBean implements Serializable{
    
    @Inject
    private NewsfeedJpaController newsFeedController;
    @Inject
    private FrontPageSettingsJpaController fpsController;
    private Newsfeed newsFeed;
    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method will return a news feed if it exists already. Otherwise, it 
     * will return a new news feed object.
     * @return news feed object
     */
    public Newsfeed getNewsFeed(){
        if(newsFeed == null){
            newsFeed = new Newsfeed();
        }
        return newsFeed;
    }
    
    /**
     * Finds the news feed from its id.
     * @param id of the news feed
     * @return news feed object
     */
    public Newsfeed findNewsFeedById(int id){
        return newsFeedController.findNewsfeed(id);
    }
    
    /**
     * This method will return all the news feeds in the database so it can be 
     * displayed on the data table.
     * @return list of all the news feed
     */
    public List<Newsfeed> getAll()
    {
        return newsFeedController.findNewsfeedEntities();
    }
    
    /**
     * This method will save the news feed to the database and select
     * it so that the rss feed manager can change the news feed that is 
     * being displayed on the main page. Then, it sets the news feed to null
     * so that it does not stay in the session scoped.
     * @return null should make it stay on the same page
     */
    public String save()
    {
        try
        {
            newsFeedController.create(newsFeed);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }       
        
        this.newsFeed = null;
        return null;
    }
    
    /**
     * This method will destroy the news feed in the database and it sets the 
     * news feed object to null so that it does not stay in session scoped.
     *
     * @param id of the news feed object
     * @return null should make it stay on the same page
     */
    public String remove(Integer id)
    {
        //it is used on the front page don't change it unless you select another one
          if(fpsController.findFrontPageSettings(1).getNewsfeedId().equals(newsFeedController.findNewsfeed(id)))
            return null;
        try
        {
            newsFeedController.destroy(id);
        }
        
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        this.newsFeed = null;
        return null;
    }
    
    /**
     * This method will find the news feed from its id and use the feedManager
     * to change the news feed being displayed on the main page. Lastly, it
     * sets the news feed object to null so that it does not stay in session 
     * scooped.
     * @param id of the news feed object
     * @return null should make it stay on the same page
     */
    public String select(Integer id)
    {
        newsFeed = newsFeedController.findNewsfeed(id);
        
        FrontPageSettings fps = fpsController.findFrontPageSettings(1);
        fps.setNewsfeedId(newsFeed);
        
        try
        {
            fpsController.edit(fps);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        
        this.newsFeed = null;
        return null;
    }
    
    public void setNewsFeed(Newsfeed newsFeed)
    {
        this.newsFeed = newsFeed;
    }
    
    
    
}

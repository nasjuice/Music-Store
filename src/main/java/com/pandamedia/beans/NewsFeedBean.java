package com.pandamedia.beans;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.controllers.FrontPageSettingsJpaController;
import persistence.controllers.NewsFeedActionController;
import persistence.entities.FrontPageSettings;
import persistence.entities.Newsfeed;

/**
 * Holds the URL of desired RSSFeed
 * @author Hau Gilles Che
 */
@Named
@RequestScoped
public class NewsFeedBean implements Serializable {
    //private String url="http://rss.cbc.ca/lineup/politics.xml";
    @Inject
    private NewsFeedActionController newsFeedActionController;
    private Newsfeed newsFeed;
    @Inject
    private FrontPageSettingsJpaController fpsController;
    
    @PostConstruct
    public void init(){
        //newsFeed = newsFeedActionController.getCurrentNewsFeed();
        FrontPageSettings fps = fpsController.findFrontPageSettings(1);
        newsFeed = fps.getNewsfeedId();
    }
    
    
    /*
    public NewsFeedBean(){
        //doesnt seem to work
        url="http://rss.cbc.ca/lineup/politics.xml";
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    */

    public Newsfeed getNewsFeed() {
        return newsFeed;
    }

    public void setNewsFeed(Newsfeed newsFeed) {
        this.newsFeed = newsFeed;
    }
}

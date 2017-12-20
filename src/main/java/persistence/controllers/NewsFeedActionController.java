package persistence.controllers;

import com.pandamedia.beans.NewsFeedBean;
import javax.inject.Inject;
import persistence.entities.FrontPageSettings;
import persistence.entities.Newsfeed;

/**
 *
 * @author Hau Gilles Che
 */
public class NewsFeedActionController {
    @Inject
    private FrontPageSettingsJpaController frontPageController;
    
    /**
     * 
     * @return current RSSFeed to use.
     */
    public Newsfeed getCurrentNewsFeed(){
        FrontPageSettings frontPage = frontPageController
                .findFrontPageSettings(1);
        
        Newsfeed newsFeed=frontPage.getNewsfeedId();
        return newsFeed;
    }
}

package persistence.controllers;

import javax.inject.Inject;
import persistence.entities.FrontPageSettings;
import persistence.entities.Survey;

/**
 *
 * @author Hau Gilles Che
 */
public class SurveyActionController {
    @Inject
    private SurveyJpaController surveyJpaController;
    @Inject
    private FrontPageSettingsJpaController frontPageJpaController;

    
    /**
     * 
     * @return the survey to be displayed to the user.
     */
    public Survey getCurrentSurvey(){
        FrontPageSettings frontPageSettings=frontPageJpaController
                .findFrontPageSettings(1);
        
        Survey survey=frontPageSettings.getSurveyId();
        return survey;      
    }
}

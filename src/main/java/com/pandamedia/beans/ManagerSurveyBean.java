package com.pandamedia.beans;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.controllers.FrontPageSettingsJpaController;
import persistence.controllers.SurveyJpaController;
import persistence.entities.FrontPageSettings;
import persistence.entities.Survey;

/**
 * This class will be used as the survey backing bean. It is used as a means of
 * getting surveys and querying them.
 *
 * @author Naasir
 */
@Named("managerSurveyBean")
@SessionScoped
public class ManagerSurveyBean implements Serializable {

    @Inject
    private SurveyJpaController surveyController;
    @Inject
    private FrontPageSettingsJpaController fpsController;

    private Survey survey;
    private boolean removeException = false;

    /**
     * Finds the survey from its id.
     *
     * @param id of the survey
     * @return survey object
     */
    public Survey findSurveyById(int id) {
        return surveyController.findSurvey(id);
    }

    public boolean isRemoveException() {
        return removeException;
    }

    /**
     * This method will save the survey to the database and select it so that
     * the manager can change the survey that is being displayed on the main
     * page.
     *
     * @return null should make it stay on the same page
     */
    public String save() {
        try {
            surveyController.create(survey);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.survey = null;
        return null;
    }

    /**
     * This method will destroy the survey in the database and it sets the
     * survey object to null so that it does not stay in session scoped.
     *
     * @param id of the survey object
     * @return null should make it stay on the same page
     */
    public String remove(Integer id) {
            //it is used on the front page don't change it unless you select another one
        if (fpsController.findFrontPageSettings(1).getSurveyId().equals(surveyController.findSurvey(id))) 
        return null;
        try {
            surveyController.destroy(id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.survey = null;
        return null;
    }

    /**
     * This method will find the survey from its id and set it in order to
     * change the survey being displayed on the main page.
     *
     * @param id of the survey object
     * @return null should make it stay on the same page
     */
    public String select(Integer id) {
        survey = surveyController.findSurvey(id);

        FrontPageSettings fps = fpsController.findFrontPageSettings(1);
        fps.setSurveyId(survey);

        try {
            fpsController.edit(fps);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        this.survey = null;
        return null;
    }

    /**
     * This method will return a survey if it exists already. Otherwise, it will
     * return a new survey object.
     *
     * @return survey object
     */
    public Survey getSurvey() {
        if (survey == null) {
            survey = new Survey();
        }
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    /**
     * This method will return all the surveys in the database so it can be
     * displayed on the data table.
     *
     * @return list of all the surveys
     */
    public List<Survey> getAll() {
        return surveyController.findSurveyEntities();
    }
}

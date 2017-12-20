package com.pandamedia.beans;

import persistence.controllers.SurveyJpaController;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Survey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.controllers.FrontPageSettingsJpaController;
import persistence.controllers.SurveyActionController;
import persistence.entities.FrontPageSettings;

/**
 * This class will be used as the survey backing bean. It is used as a means
 * of getting surveys and querying them.
 * @author Hau Gilles Che
 */
@Named("surveyBean")
@SessionScoped
public class SurveyBean implements Serializable {

    private int surveyId=1;
    private String userChoice;
    private Survey survey;
    private List<String> answers;
    private boolean userAnswered;
    private boolean showOptions;
    
    @Inject
    private FrontPageSettingsJpaController fpsController;
    @Inject
    private SurveyJpaController surveys;
   
    @PostConstruct
    public void init()
    {
        survey = fpsController.findFrontPageSettings(1).getSurveyId();
        createAnswerList();
        userAnswered = false;
        showOptions = true;
    }
    
    /**
     * 
     * @return survey record from database
     */
    public Survey getSurvey()
    {
        if(!fpsController.findFrontPageSettings(1).getSurveyId().equals(survey))
        {
            survey = fpsController.findFrontPageSettings(1).getSurveyId();
            createAnswerList();
            userAnswered = false;
            showOptions = true;
        }
        
        return survey;
    }
       
    /**
     * 
     * @return the choice selected by the user.
     */
    public String getUserChoice() {
        return userChoice;
    }

    /**
     * 
     * @param userChoice value to be set as user choice.
     */
    public void setUserChoice(String userChoice) {
        this.userChoice = userChoice;
    }

    /**
     * 
     * @return id of the current survey.
     */
    public int getSurveyId() {
        return surveyId;
    }

    /**
     * 
     * @param surveyId value to set as survey id.
     */
    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }

    /**
     * 
     * @param survey survey object to be set.
     */
    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    /**
     * 
     * @return a list of survey answers (choices).
     */
    public List<String> getAnswers() {
        return answers;
    }

    /**
     * 
     * @param answers list of string values to set as survey answers.
     */
    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    /**
     * 
     * @return true if the answers (choices) are to be visible.
     */
    public boolean isShowOptions() {
        return showOptions;
    }

    public void setShowOptions(boolean showOptions) {
        this.showOptions = showOptions;
    }
    
    
    
    public int getTotalVotes(){
        int totalVotes=0;
        totalVotes+=survey.getVotesA();
        totalVotes+=survey.getVotesB();
        totalVotes+=survey.getVotesC();
        totalVotes+=survey.getVotesD();
        
        return totalVotes;
    }

    public boolean isUserAnswered() {
        return userAnswered;
    }

    public void setUserAnswered(boolean userAnswered) {
        this.userAnswered = userAnswered;
    }
    
    
    
    public String updateSurvey() throws Exception{
        incrementVoteCount();
        userAnswered=true;
        showOptions=false;
        return null;
    }
    
    public void incrementVoteCount() throws RollbackFailureException, Exception{
        //surveyResults.setDisplayed(true);
      int voteNum=0;
      if(userChoice.equalsIgnoreCase(survey.getAnswerA())){
          voteNum=survey.getVotesA();
          voteNum++;
          survey.setVotesA(voteNum);
      }else if(userChoice.equalsIgnoreCase(survey.getAnswerB())){
          voteNum=survey.getVotesB();
          voteNum++;
          survey.setVotesB(voteNum);
      }else if(userChoice.equalsIgnoreCase(survey.getAnswerC())){
          voteNum=survey.getVotesC();
          voteNum++;
          survey.setVotesC(voteNum);
      }else{
          voteNum=survey.getVotesD();
          voteNum++;
          survey.setVotesD(voteNum);
      }
      surveys.edit(survey);
    }
    
    private void createAnswerList(){
        answers=new ArrayList<>();
        answers.add(survey.getAnswerA());
        answers.add(survey.getAnswerB());
        answers.add(survey.getAnswerC());
        answers.add(survey.getAnswerD());
    }
    
    private int getPercentageVote(int numVote){
        int total=getTotalVotes();
        int vote = (numVote*100) / total;
        System.out.println("PERCENT:"+vote+"NUMVOTE: "+numVote+" TOTAL:"+total);
        return vote;
    }
    
    public int getVotesA(){
        return getPercentageVote(survey.getVotesA());
    }
    
    public int getVotesB(){
        return getPercentageVote(survey.getVotesB());
    }
    
    public int getVotesC(){
        return getPercentageVote(survey.getVotesC());
    }
    
    public int getVotesD(){
        return getPercentageVote(survey.getVotesD());
    }
}

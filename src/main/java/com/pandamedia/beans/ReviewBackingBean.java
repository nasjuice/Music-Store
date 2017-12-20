

package com.pandamedia.beans;

import persistence.controllers.ReviewJpaController;
import persistence.entities.Review;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import persistence.entities.ShopUser;
import persistence.entities.Track;



/**
 * This class will be used as the review backing bean. It can create, update,
 * delete and query reviews.
 * @author Evan Glicakis, Naasir Jusab
 */
@Named("reviewBacking")
@SessionScoped
public class ReviewBackingBean implements Serializable{
    @Inject
    private ReviewJpaController reviewController;
    private Review review;
    private List<Review> reviews;
    private List<Review> filteredReviews;
    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method will initialize a list of reviews that will be used by the 
     * data table. PostConstruct is used in methods that need to be executed after 
     * dependency injection is done to perform any initialization. In this case,
     * I need the list of reviews after reviewController has been injected.
     */
    @PostConstruct
    public void init()
    {
        this.reviews = reviewController.findReviewEntities();     
    }
    
    /**
     * This method will return all the reviews in a list so it can be displayed
     * on the data table.
     * @return all reviews in the database
     */
    public List<Review> getReviews()
    {
        return reviews;
    }
    
    /**
     * This method will set a list of reviews to make changes to the current
     * list of all reviews.
     * @param reviews all reviews in the database
     */
    public void setReviews(List<Review> reviews)
    {
        this.reviews = reviews;
    }
    
    /**
     * This method will set a list of filtered reviews to change the current
     * list of filtered reviews.
     * @param filteredReviews list of filtered reviews
     */
    public void setFilteredReviews(List<Review> filteredReviews)
    {
        this.filteredReviews = filteredReviews;
    }
    
    /**
     * This method will return a list of filtered reviews so that the manager
     * can make searches on reviews.
     * @return list of filteredReviews
     */
    public List<Review> getFilteredReviews()
    {
        return this.filteredReviews;
    }
    
    /**
     * This method will return a review if it exists already. Otherwise, it will
     * return a new review.
     * @return review
     */
    public Review getReview(){
        if(review == null){
            review = new Review();
        }
        return review;
    }
    
    /**
     * Finds the review from its id.
     * @param id of the review
     * @return review object
     */
    public Review findReviewById(int id){
        return reviewController.findReview(id); 
    }
    
    /**
     * This method takes the id of a review to search for that review object.
     * Then,it will remove it completely from the database. At the end, the 
     * review is set to null so that it does not stay in session scoped and the
     * filtered reviews are regenerated. The return type null should make it 
     * stay on the same page.
     * @param id of the review object
     * @return null make it stay on the same page
     */
    public String removeItem(Integer id) 
    {
        
        review = reviewController.findReview(id);
        
        try
        {
            reviewController.destroy(review.getId());
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        
        this.review = null;
        this.reviews = reviewController.findReviewEntities();
        this.filteredReviews = reviewController.findReviewEntities();
        return null; 
    }
    
    /**
     * This method takes the id of a review to search for that review object.
     * If the approval status is not 1 then it will change it to 1 which,
     * signifies that it has been approved. 0 means that the review has not
     * been approved. At the end, the review is set to null so that it does not  
     * stay in session scoped and the filtered reviews are regenerated. The  
     * return type null should make it stay on the same page.
     * @param id of the review object
     * @return null make it stay on the same page
     */
    public String approve(Integer id)
    {
        review = reviewController.findReview(id);
        
        if(review.getApprovalStatus() != 1)
        {
            short i = 1;
            review.setApprovalStatus(i);

            try
            {
                reviewController.edit(review);
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }                  
        }
        this.review = null;
        this.reviews = reviewController.findReviewEntities();
        this.filteredReviews = reviewController.findReviewEntities();
        return null;
    }
    
    /**
     * This method takes the id of a review to search for that review object.
     * If the approval status is not 0 then it will change it to 0 which,
     * signifies that it has been disapproved. 1 means that the review has 
     * been approved. At the end, the review is set to null so that it does not  
     * stay in session scoped and the filtered reviews are regenerated. The  
     * return type null should make it stay on the same page.
     * @param id of the review object
     * @return null make it stay on the same page
     */
    public String disapprove(Integer id)
    {
        review = reviewController.findReview(id);
        
        if(review.getApprovalStatus() != 0)
        {
            short i = 0;
            review.setApprovalStatus(i);

            try
            {
                reviewController.edit(review);
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
                    
        }
        this.review = null;
        this.reviews = reviewController.findReviewEntities();
        this.filteredReviews = reviewController.findReviewEntities();
        return null;
    }
    /**
     * Action used by command button to submit the review, originally return void
     * to be used by ajax without reloading the page.
     * @param track
     * @param su 
     */
    public void submitReview(Track track, ShopUser su){
        Review r = new Review();
        short approvalStatus = 0; // review is not approved.
        r.setApprovalStatus(approvalStatus);
        Date currentDate = new Date(); // get current date.
        r.setDateEntered(currentDate);
        r.setReviewContent(this.review.getReviewContent());
        r.setRating(this.review.getRating());
        r.setTrackId(track);
        r.setUserId(su);
        
        System.out.println("id: " + r.getId() + "\n" +
        "approval status: " + r.getApprovalStatus() + "\n" +
        "curr date: " + r.getDateEntered() + "\n" +
        "content: " + r.getReviewContent() + "\n" +
        "rating: " + r.getRating() + "\n" +
        "submitted by: " + r.getUserId().getEmail() + "\n" +
        "submitted for: " + r.getTrackId().getTitle());
        
        try {
            reviewController.create(r);
        } catch (Exception ex) {
            Logger.getLogger(ReviewBackingBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        // set the review content and rating to their default state so the form can be cleared.
        this.review.setReviewContent(null);
        this.review.setRating(1);
    }

    /**
     * validation rule for review content field.
     * @param fc
     * @param c
     * @param obj 
     */
    public void validateReviewContent(FacesContext fc, UIComponent c, Object obj){
        if(obj == null){
            review.setReviewContent("Review message is empty");
            throw new ValidatorException(new FacesMessage("Review message is empty."));
        }
        String content = (String)obj;
        if(content.length() < 3 || content.length() > 2000){
            throw new ValidatorException(new FacesMessage("Your review must be between 3 and 2000 characters."));
        }
    }
    
    /**
     * This method will return all the reviews in the database so it can be
     * displayed on the data table.
     * @return list of reviews
     */
    public List<Review> getAll()
    {
        return reviewController.findReviewEntities();
    }
    
    /**
     *
     * @param fc
     * @param c
     * @param obj
     */
    public void validateRating(FacesContext fc, UIComponent c, Object obj){
        if(obj == null){
            throw new ValidatorException(new FacesMessage("You must rate the track."));
        }
    }
    
    public void setReview(Review review)
    {
        this.review = review;
    }
    
}


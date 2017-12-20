package persistence.controllers;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import persistence.controllers.exceptions.NonexistentEntityException;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Review;
import persistence.entities.Track;
import persistence.entities.ShopUser;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class ReviewJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public ReviewJpaController() {
    }

    public void create(Review review) throws RollbackFailureException, Exception {
        try {
            utx.begin();
            Track trackId = review.getTrackId();
            if (trackId != null) {
                trackId = em.getReference(trackId.getClass(), trackId.getId());
                review.setTrackId(trackId);
            }
            ShopUser userId = review.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                review.setUserId(userId);
            }
            em.persist(review);
            if (trackId != null) {
                trackId.getReviewList().add(review);
                trackId = em.merge(trackId);
            }
            if (userId != null) {
                userId.getReviewList().add(review);
                userId = em.merge(userId);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        }
    }

    public void edit(Review review) throws NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Review persistentReview = em.find(Review.class, review.getId());
            Track trackIdOld = persistentReview.getTrackId();
            Track trackIdNew = review.getTrackId();
            ShopUser userIdOld = persistentReview.getUserId();
            ShopUser userIdNew = review.getUserId();
            if (trackIdNew != null) {
                trackIdNew = em.getReference(trackIdNew.getClass(), trackIdNew.getId());
                review.setTrackId(trackIdNew);
            }
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                review.setUserId(userIdNew);
            }
            review = em.merge(review);
            if (trackIdOld != null && !trackIdOld.equals(trackIdNew)) {
                trackIdOld.getReviewList().remove(review);
                trackIdOld = em.merge(trackIdOld);
            }
            if (trackIdNew != null && !trackIdNew.equals(trackIdOld)) {
                trackIdNew.getReviewList().add(review);
                trackIdNew = em.merge(trackIdNew);
            }
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getReviewList().remove(review);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getReviewList().add(review);
                userIdNew = em.merge(userIdNew);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = review.getId();
                if (findReview(id) == null) {
                    throw new NonexistentEntityException("The review with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Review review;
            try {
                review = em.getReference(Review.class, id);
                review.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The review with id " + id + " no longer exists.", enfe);
            }
            Track trackId = review.getTrackId();
            if (trackId != null) {
                trackId.getReviewList().remove(review);
                trackId = em.merge(trackId);
            }
            ShopUser userId = review.getUserId();
            if (userId != null) {
                userId.getReviewList().remove(review);
                userId = em.merge(userId);
            }
            em.remove(review);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        }
    }

    public List<Review> findReviewEntities() {
        return findReviewEntities(true, -1, -1);
    }

    public List<Review> findReviewEntities(int maxResults, int firstResult) {
        return findReviewEntities(false, maxResults, firstResult);
    }

    private List<Review> findReviewEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Review.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Review findReview(Integer id) {
        return em.find(Review.class, id);
    }

    public int getReviewCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Review> rt = cq.from(Review.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

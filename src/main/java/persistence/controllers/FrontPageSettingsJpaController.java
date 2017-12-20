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
import persistence.entities.Survey;
import persistence.entities.Newsfeed;
import persistence.entities.Advertisement;
import persistence.entities.FrontPageSettings;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class FrontPageSettingsJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public FrontPageSettingsJpaController() {
    }

    public void create(FrontPageSettings frontPageSettings) throws RollbackFailureException, Exception {
        try {
            utx.begin();
            Survey surveyId = frontPageSettings.getSurveyId();
            if (surveyId != null) {
                surveyId = em.getReference(surveyId.getClass(), surveyId.getId());
                frontPageSettings.setSurveyId(surveyId);
            }
            Newsfeed newsfeedId = frontPageSettings.getNewsfeedId();
            if (newsfeedId != null) {
                newsfeedId = em.getReference(newsfeedId.getClass(), newsfeedId.getId());
                frontPageSettings.setNewsfeedId(newsfeedId);
            }
            Advertisement adAId = frontPageSettings.getAdAId();
            if (adAId != null) {
                adAId = em.getReference(adAId.getClass(), adAId.getId());
                frontPageSettings.setAdAId(adAId);
            }
            em.persist(frontPageSettings);
            if (surveyId != null) {
                surveyId.getFrontPageSettingsList().add(frontPageSettings);
                surveyId = em.merge(surveyId);
            }
            if (newsfeedId != null) {
                newsfeedId.getFrontPageSettingsList().add(frontPageSettings);
                newsfeedId = em.merge(newsfeedId);
            }
            if (adAId != null) {
                adAId.getFrontPageSettingsList().add(frontPageSettings);
                adAId = em.merge(adAId);
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

    public void edit(FrontPageSettings frontPageSettings) throws NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            FrontPageSettings persistentFrontPageSettings = em.find(FrontPageSettings.class, frontPageSettings.getId());
            Survey surveyIdOld = persistentFrontPageSettings.getSurveyId();
            Survey surveyIdNew = frontPageSettings.getSurveyId();
            Newsfeed newsfeedIdOld = persistentFrontPageSettings.getNewsfeedId();
            Newsfeed newsfeedIdNew = frontPageSettings.getNewsfeedId();
            Advertisement adAIdOld = persistentFrontPageSettings.getAdAId();
            Advertisement adAIdNew = frontPageSettings.getAdAId();
            if (surveyIdNew != null) {
                surveyIdNew = em.getReference(surveyIdNew.getClass(), surveyIdNew.getId());
                frontPageSettings.setSurveyId(surveyIdNew);
            }
            if (newsfeedIdNew != null) {
                newsfeedIdNew = em.getReference(newsfeedIdNew.getClass(), newsfeedIdNew.getId());
                frontPageSettings.setNewsfeedId(newsfeedIdNew);
            }
            if (adAIdNew != null) {
                adAIdNew = em.getReference(adAIdNew.getClass(), adAIdNew.getId());
                frontPageSettings.setAdAId(adAIdNew);
            }
            frontPageSettings = em.merge(frontPageSettings);
            if (surveyIdOld != null && !surveyIdOld.equals(surveyIdNew)) {
                surveyIdOld.getFrontPageSettingsList().remove(frontPageSettings);
                surveyIdOld = em.merge(surveyIdOld);
            }
            if (surveyIdNew != null && !surveyIdNew.equals(surveyIdOld)) {
                surveyIdNew.getFrontPageSettingsList().add(frontPageSettings);
                surveyIdNew = em.merge(surveyIdNew);
            }
            if (newsfeedIdOld != null && !newsfeedIdOld.equals(newsfeedIdNew)) {
                newsfeedIdOld.getFrontPageSettingsList().remove(frontPageSettings);
                newsfeedIdOld = em.merge(newsfeedIdOld);
            }
            if (newsfeedIdNew != null && !newsfeedIdNew.equals(newsfeedIdOld)) {
                newsfeedIdNew.getFrontPageSettingsList().add(frontPageSettings);
                newsfeedIdNew = em.merge(newsfeedIdNew);
            }
            if (adAIdOld != null && !adAIdOld.equals(adAIdNew)) {
                adAIdOld.getFrontPageSettingsList().remove(frontPageSettings);
                adAIdOld = em.merge(adAIdOld);
            }
            if (adAIdNew != null && !adAIdNew.equals(adAIdOld)) {
                adAIdNew.getFrontPageSettingsList().add(frontPageSettings);
                adAIdNew = em.merge(adAIdNew);
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
                Integer id = frontPageSettings.getId();
                if (findFrontPageSettings(id) == null) {
                    throw new NonexistentEntityException("The frontPageSettings with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            FrontPageSettings frontPageSettings;
            try {
                frontPageSettings = em.getReference(FrontPageSettings.class, id);
                frontPageSettings.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The frontPageSettings with id " + id + " no longer exists.", enfe);
            }
            Survey surveyId = frontPageSettings.getSurveyId();
            if (surveyId != null) {
                surveyId.getFrontPageSettingsList().remove(frontPageSettings);
                surveyId = em.merge(surveyId);
            }
            Newsfeed newsfeedId = frontPageSettings.getNewsfeedId();
            if (newsfeedId != null) {
                newsfeedId.getFrontPageSettingsList().remove(frontPageSettings);
                newsfeedId = em.merge(newsfeedId);
            }
            Advertisement adAId = frontPageSettings.getAdAId();
            if (adAId != null) {
                adAId.getFrontPageSettingsList().remove(frontPageSettings);
                adAId = em.merge(adAId);
            }
            em.remove(frontPageSettings);
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

    public List<FrontPageSettings> findFrontPageSettingsEntities() {
        return findFrontPageSettingsEntities(true, -1, -1);
    }

    public List<FrontPageSettings> findFrontPageSettingsEntities(int maxResults, int firstResult) {
        return findFrontPageSettingsEntities(false, maxResults, firstResult);
    }

    private List<FrontPageSettings> findFrontPageSettingsEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(FrontPageSettings.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public FrontPageSettings findFrontPageSettings(Integer id) {
        return em.find(FrontPageSettings.class, id);
    }

    public int getFrontPageSettingsCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<FrontPageSettings> rt = cq.from(FrontPageSettings.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

package persistence.controllers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import persistence.entities.FrontPageSettings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import persistence.controllers.exceptions.IllegalOrphanException;
import persistence.controllers.exceptions.NonexistentEntityException;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Survey;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class SurveyJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public SurveyJpaController() {
    }

    public void create(Survey survey) throws RollbackFailureException, Exception {
        if (survey.getFrontPageSettingsList() == null) {
            survey.setFrontPageSettingsList(new ArrayList<FrontPageSettings>());
        }
        try {
            utx.begin();
            List<FrontPageSettings> attachedFrontPageSettingsList = new ArrayList<FrontPageSettings>();
            for (FrontPageSettings frontPageSettingsListFrontPageSettingsToAttach : survey.getFrontPageSettingsList()) {
                frontPageSettingsListFrontPageSettingsToAttach = em.getReference(frontPageSettingsListFrontPageSettingsToAttach.getClass(), frontPageSettingsListFrontPageSettingsToAttach.getId());
                attachedFrontPageSettingsList.add(frontPageSettingsListFrontPageSettingsToAttach);
            }
            survey.setFrontPageSettingsList(attachedFrontPageSettingsList);
            em.persist(survey);
            for (FrontPageSettings frontPageSettingsListFrontPageSettings : survey.getFrontPageSettingsList()) {
                Survey oldSurveyIdOfFrontPageSettingsListFrontPageSettings = frontPageSettingsListFrontPageSettings.getSurveyId();
                frontPageSettingsListFrontPageSettings.setSurveyId(survey);
                frontPageSettingsListFrontPageSettings = em.merge(frontPageSettingsListFrontPageSettings);
                if (oldSurveyIdOfFrontPageSettingsListFrontPageSettings != null) {
                    oldSurveyIdOfFrontPageSettingsListFrontPageSettings.getFrontPageSettingsList().remove(frontPageSettingsListFrontPageSettings);
                    oldSurveyIdOfFrontPageSettingsListFrontPageSettings = em.merge(oldSurveyIdOfFrontPageSettingsListFrontPageSettings);
                }
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

    public void edit(Survey survey) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Survey persistentSurvey = em.find(Survey.class, survey.getId());
            List<FrontPageSettings> frontPageSettingsListOld = persistentSurvey.getFrontPageSettingsList();
            List<FrontPageSettings> frontPageSettingsListNew = survey.getFrontPageSettingsList();
            List<String> illegalOrphanMessages = null;
            for (FrontPageSettings frontPageSettingsListOldFrontPageSettings : frontPageSettingsListOld) {
                if (!frontPageSettingsListNew.contains(frontPageSettingsListOldFrontPageSettings)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain FrontPageSettings " + frontPageSettingsListOldFrontPageSettings + " since its surveyId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<FrontPageSettings> attachedFrontPageSettingsListNew = new ArrayList<FrontPageSettings>();
            for (FrontPageSettings frontPageSettingsListNewFrontPageSettingsToAttach : frontPageSettingsListNew) {
                frontPageSettingsListNewFrontPageSettingsToAttach = em.getReference(frontPageSettingsListNewFrontPageSettingsToAttach.getClass(), frontPageSettingsListNewFrontPageSettingsToAttach.getId());
                attachedFrontPageSettingsListNew.add(frontPageSettingsListNewFrontPageSettingsToAttach);
            }
            frontPageSettingsListNew = attachedFrontPageSettingsListNew;
            survey.setFrontPageSettingsList(frontPageSettingsListNew);
            survey = em.merge(survey);
            for (FrontPageSettings frontPageSettingsListNewFrontPageSettings : frontPageSettingsListNew) {
                if (!frontPageSettingsListOld.contains(frontPageSettingsListNewFrontPageSettings)) {
                    Survey oldSurveyIdOfFrontPageSettingsListNewFrontPageSettings = frontPageSettingsListNewFrontPageSettings.getSurveyId();
                    frontPageSettingsListNewFrontPageSettings.setSurveyId(survey);
                    frontPageSettingsListNewFrontPageSettings = em.merge(frontPageSettingsListNewFrontPageSettings);
                    if (oldSurveyIdOfFrontPageSettingsListNewFrontPageSettings != null && !oldSurveyIdOfFrontPageSettingsListNewFrontPageSettings.equals(survey)) {
                        oldSurveyIdOfFrontPageSettingsListNewFrontPageSettings.getFrontPageSettingsList().remove(frontPageSettingsListNewFrontPageSettings);
                        oldSurveyIdOfFrontPageSettingsListNewFrontPageSettings = em.merge(oldSurveyIdOfFrontPageSettingsListNewFrontPageSettings);
                    }
                }
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
                Integer id = survey.getId();
                if (findSurvey(id) == null) {
                    throw new NonexistentEntityException("The survey with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Survey survey;
            try {
                survey = em.getReference(Survey.class, id);
                survey.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The survey with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<FrontPageSettings> frontPageSettingsListOrphanCheck = survey.getFrontPageSettingsList();
            for (FrontPageSettings frontPageSettingsListOrphanCheckFrontPageSettings : frontPageSettingsListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Survey (" + survey + ") cannot be destroyed since the FrontPageSettings " + frontPageSettingsListOrphanCheckFrontPageSettings + " in its frontPageSettingsList field has a non-nullable surveyId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(survey);
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

    public List<Survey> findSurveyEntities() {
        return findSurveyEntities(true, -1, -1);
    }

    public List<Survey> findSurveyEntities(int maxResults, int firstResult) {
        return findSurveyEntities(false, maxResults, firstResult);
    }

    private List<Survey> findSurveyEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Survey.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Survey findSurvey(Integer id) {
        return em.find(Survey.class, id);
    }

    public int getSurveyCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Survey> rt = cq.from(Survey.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

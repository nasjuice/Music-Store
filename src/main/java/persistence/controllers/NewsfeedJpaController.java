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
import persistence.entities.Newsfeed;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class NewsfeedJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public NewsfeedJpaController() {
    }

    public void create(Newsfeed newsfeed) throws RollbackFailureException, Exception {
        if (newsfeed.getFrontPageSettingsList() == null) {
            newsfeed.setFrontPageSettingsList(new ArrayList<FrontPageSettings>());
        }
        try {
            utx.begin();
            List<FrontPageSettings> attachedFrontPageSettingsList = new ArrayList<FrontPageSettings>();
            for (FrontPageSettings frontPageSettingsListFrontPageSettingsToAttach : newsfeed.getFrontPageSettingsList()) {
                frontPageSettingsListFrontPageSettingsToAttach = em.getReference(frontPageSettingsListFrontPageSettingsToAttach.getClass(), frontPageSettingsListFrontPageSettingsToAttach.getId());
                attachedFrontPageSettingsList.add(frontPageSettingsListFrontPageSettingsToAttach);
            }
            newsfeed.setFrontPageSettingsList(attachedFrontPageSettingsList);
            em.persist(newsfeed);
            for (FrontPageSettings frontPageSettingsListFrontPageSettings : newsfeed.getFrontPageSettingsList()) {
                Newsfeed oldNewsfeedIdOfFrontPageSettingsListFrontPageSettings = frontPageSettingsListFrontPageSettings.getNewsfeedId();
                frontPageSettingsListFrontPageSettings.setNewsfeedId(newsfeed);
                frontPageSettingsListFrontPageSettings = em.merge(frontPageSettingsListFrontPageSettings);
                if (oldNewsfeedIdOfFrontPageSettingsListFrontPageSettings != null) {
                    oldNewsfeedIdOfFrontPageSettingsListFrontPageSettings.getFrontPageSettingsList().remove(frontPageSettingsListFrontPageSettings);
                    oldNewsfeedIdOfFrontPageSettingsListFrontPageSettings = em.merge(oldNewsfeedIdOfFrontPageSettingsListFrontPageSettings);
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

    public void edit(Newsfeed newsfeed) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Newsfeed persistentNewsfeed = em.find(Newsfeed.class, newsfeed.getId());
            List<FrontPageSettings> frontPageSettingsListOld = persistentNewsfeed.getFrontPageSettingsList();
            List<FrontPageSettings> frontPageSettingsListNew = newsfeed.getFrontPageSettingsList();
            List<String> illegalOrphanMessages = null;
            for (FrontPageSettings frontPageSettingsListOldFrontPageSettings : frontPageSettingsListOld) {
                if (!frontPageSettingsListNew.contains(frontPageSettingsListOldFrontPageSettings)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain FrontPageSettings " + frontPageSettingsListOldFrontPageSettings + " since its newsfeedId field is not nullable.");
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
            newsfeed.setFrontPageSettingsList(frontPageSettingsListNew);
            newsfeed = em.merge(newsfeed);
            for (FrontPageSettings frontPageSettingsListNewFrontPageSettings : frontPageSettingsListNew) {
                if (!frontPageSettingsListOld.contains(frontPageSettingsListNewFrontPageSettings)) {
                    Newsfeed oldNewsfeedIdOfFrontPageSettingsListNewFrontPageSettings = frontPageSettingsListNewFrontPageSettings.getNewsfeedId();
                    frontPageSettingsListNewFrontPageSettings.setNewsfeedId(newsfeed);
                    frontPageSettingsListNewFrontPageSettings = em.merge(frontPageSettingsListNewFrontPageSettings);
                    if (oldNewsfeedIdOfFrontPageSettingsListNewFrontPageSettings != null && !oldNewsfeedIdOfFrontPageSettingsListNewFrontPageSettings.equals(newsfeed)) {
                        oldNewsfeedIdOfFrontPageSettingsListNewFrontPageSettings.getFrontPageSettingsList().remove(frontPageSettingsListNewFrontPageSettings);
                        oldNewsfeedIdOfFrontPageSettingsListNewFrontPageSettings = em.merge(oldNewsfeedIdOfFrontPageSettingsListNewFrontPageSettings);
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
                Integer id = newsfeed.getId();
                if (findNewsfeed(id) == null) {
                    throw new NonexistentEntityException("The newsfeed with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Newsfeed newsfeed;
            try {
                newsfeed = em.getReference(Newsfeed.class, id);
                newsfeed.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The newsfeed with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<FrontPageSettings> frontPageSettingsListOrphanCheck = newsfeed.getFrontPageSettingsList();
            for (FrontPageSettings frontPageSettingsListOrphanCheckFrontPageSettings : frontPageSettingsListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Newsfeed (" + newsfeed + ") cannot be destroyed since the FrontPageSettings " + frontPageSettingsListOrphanCheckFrontPageSettings + " in its frontPageSettingsList field has a non-nullable newsfeedId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(newsfeed);
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

    public List<Newsfeed> findNewsfeedEntities() {
        return findNewsfeedEntities(true, -1, -1);
    }

    public List<Newsfeed> findNewsfeedEntities(int maxResults, int firstResult) {
        return findNewsfeedEntities(false, maxResults, firstResult);
    }

    private List<Newsfeed> findNewsfeedEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Newsfeed.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Newsfeed findNewsfeed(Integer id) {
        return em.find(Newsfeed.class, id);
    }

    public int getNewsfeedCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Newsfeed> rt = cq.from(Newsfeed.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

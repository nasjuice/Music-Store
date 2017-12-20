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
import persistence.entities.Advertisement;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class AdvertisementJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public AdvertisementJpaController() {
    }

    public void create(Advertisement advertisement) throws RollbackFailureException, Exception {
        if (advertisement.getFrontPageSettingsList() == null) {
            advertisement.setFrontPageSettingsList(new ArrayList<FrontPageSettings>());
        }
        try {
            utx.begin();
            List<FrontPageSettings> attachedFrontPageSettingsList = new ArrayList<FrontPageSettings>();
            for (FrontPageSettings frontPageSettingsListFrontPageSettingsToAttach : advertisement.getFrontPageSettingsList()) {
                frontPageSettingsListFrontPageSettingsToAttach = em.getReference(frontPageSettingsListFrontPageSettingsToAttach.getClass(), frontPageSettingsListFrontPageSettingsToAttach.getId());
                attachedFrontPageSettingsList.add(frontPageSettingsListFrontPageSettingsToAttach);
            }
            advertisement.setFrontPageSettingsList(attachedFrontPageSettingsList);
            em.persist(advertisement);
            for (FrontPageSettings frontPageSettingsListFrontPageSettings : advertisement.getFrontPageSettingsList()) {
                Advertisement oldAdAIdOfFrontPageSettingsListFrontPageSettings = frontPageSettingsListFrontPageSettings.getAdAId();
                frontPageSettingsListFrontPageSettings.setAdAId(advertisement);
                frontPageSettingsListFrontPageSettings = em.merge(frontPageSettingsListFrontPageSettings);
                if (oldAdAIdOfFrontPageSettingsListFrontPageSettings != null) {
                    oldAdAIdOfFrontPageSettingsListFrontPageSettings.getFrontPageSettingsList().remove(frontPageSettingsListFrontPageSettings);
                    oldAdAIdOfFrontPageSettingsListFrontPageSettings = em.merge(oldAdAIdOfFrontPageSettingsListFrontPageSettings);
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

    public void edit(Advertisement advertisement) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Advertisement persistentAdvertisement = em.find(Advertisement.class, advertisement.getId());
            List<FrontPageSettings> frontPageSettingsListOld = persistentAdvertisement.getFrontPageSettingsList();
            List<FrontPageSettings> frontPageSettingsListNew = advertisement.getFrontPageSettingsList();
            List<String> illegalOrphanMessages = null;
            for (FrontPageSettings frontPageSettingsListOldFrontPageSettings : frontPageSettingsListOld) {
                if (!frontPageSettingsListNew.contains(frontPageSettingsListOldFrontPageSettings)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain FrontPageSettings " + frontPageSettingsListOldFrontPageSettings + " since its adAId field is not nullable.");
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
            advertisement.setFrontPageSettingsList(frontPageSettingsListNew);
            advertisement = em.merge(advertisement);
            for (FrontPageSettings frontPageSettingsListNewFrontPageSettings : frontPageSettingsListNew) {
                if (!frontPageSettingsListOld.contains(frontPageSettingsListNewFrontPageSettings)) {
                    Advertisement oldAdAIdOfFrontPageSettingsListNewFrontPageSettings = frontPageSettingsListNewFrontPageSettings.getAdAId();
                    frontPageSettingsListNewFrontPageSettings.setAdAId(advertisement);
                    frontPageSettingsListNewFrontPageSettings = em.merge(frontPageSettingsListNewFrontPageSettings);
                    if (oldAdAIdOfFrontPageSettingsListNewFrontPageSettings != null && !oldAdAIdOfFrontPageSettingsListNewFrontPageSettings.equals(advertisement)) {
                        oldAdAIdOfFrontPageSettingsListNewFrontPageSettings.getFrontPageSettingsList().remove(frontPageSettingsListNewFrontPageSettings);
                        oldAdAIdOfFrontPageSettingsListNewFrontPageSettings = em.merge(oldAdAIdOfFrontPageSettingsListNewFrontPageSettings);
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
                Integer id = advertisement.getId();
                if (findAdvertisement(id) == null) {
                    throw new NonexistentEntityException("The advertisement with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Advertisement advertisement;
            try {
                advertisement = em.getReference(Advertisement.class, id);
                advertisement.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The advertisement with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<FrontPageSettings> frontPageSettingsListOrphanCheck = advertisement.getFrontPageSettingsList();
            for (FrontPageSettings frontPageSettingsListOrphanCheckFrontPageSettings : frontPageSettingsListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Advertisement (" + advertisement + ") cannot be destroyed since the FrontPageSettings " + frontPageSettingsListOrphanCheckFrontPageSettings + " in its frontPageSettingsList field has a non-nullable adAId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(advertisement);
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

    public List<Advertisement> findAdvertisementEntities() {
        return findAdvertisementEntities(true, -1, -1);
    }

    public List<Advertisement> findAdvertisementEntities(int maxResults, int firstResult) {
        return findAdvertisementEntities(false, maxResults, firstResult);
    }

    private List<Advertisement> findAdvertisementEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Advertisement.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Advertisement findAdvertisement(Integer id) {
        return em.find(Advertisement.class, id);
    }

    public int getAdvertisementCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Advertisement> rt = cq.from(Advertisement.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

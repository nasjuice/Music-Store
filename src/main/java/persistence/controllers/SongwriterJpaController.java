package persistence.controllers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import persistence.entities.Track;
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
import persistence.entities.Songwriter;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class SongwriterJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public SongwriterJpaController() {
    }

    public void create(Songwriter songwriter) throws RollbackFailureException, Exception {
        if (songwriter.getTrackList() == null) {
            songwriter.setTrackList(new ArrayList<Track>());
        }
        try {
            utx.begin();
            List<Track> attachedTrackList = new ArrayList<Track>();
            for (Track trackListTrackToAttach : songwriter.getTrackList()) {
                trackListTrackToAttach = em.getReference(trackListTrackToAttach.getClass(), trackListTrackToAttach.getId());
                attachedTrackList.add(trackListTrackToAttach);
            }
            songwriter.setTrackList(attachedTrackList);
            em.persist(songwriter);
            for (Track trackListTrack : songwriter.getTrackList()) {
                Songwriter oldSongwriterIdOfTrackListTrack = trackListTrack.getSongwriterId();
                trackListTrack.setSongwriterId(songwriter);
                trackListTrack = em.merge(trackListTrack);
                if (oldSongwriterIdOfTrackListTrack != null) {
                    oldSongwriterIdOfTrackListTrack.getTrackList().remove(trackListTrack);
                    oldSongwriterIdOfTrackListTrack = em.merge(oldSongwriterIdOfTrackListTrack);
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

    public void edit(Songwriter songwriter) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Songwriter persistentSongwriter = em.find(Songwriter.class, songwriter.getId());
            List<Track> trackListOld = persistentSongwriter.getTrackList();
            List<Track> trackListNew = songwriter.getTrackList();
            List<String> illegalOrphanMessages = null;
            for (Track trackListOldTrack : trackListOld) {
                if (!trackListNew.contains(trackListOldTrack)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Track " + trackListOldTrack + " since its songwriterId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Track> attachedTrackListNew = new ArrayList<Track>();
            for (Track trackListNewTrackToAttach : trackListNew) {
                trackListNewTrackToAttach = em.getReference(trackListNewTrackToAttach.getClass(), trackListNewTrackToAttach.getId());
                attachedTrackListNew.add(trackListNewTrackToAttach);
            }
            trackListNew = attachedTrackListNew;
            songwriter.setTrackList(trackListNew);
            songwriter = em.merge(songwriter);
            for (Track trackListNewTrack : trackListNew) {
                if (!trackListOld.contains(trackListNewTrack)) {
                    Songwriter oldSongwriterIdOfTrackListNewTrack = trackListNewTrack.getSongwriterId();
                    trackListNewTrack.setSongwriterId(songwriter);
                    trackListNewTrack = em.merge(trackListNewTrack);
                    if (oldSongwriterIdOfTrackListNewTrack != null && !oldSongwriterIdOfTrackListNewTrack.equals(songwriter)) {
                        oldSongwriterIdOfTrackListNewTrack.getTrackList().remove(trackListNewTrack);
                        oldSongwriterIdOfTrackListNewTrack = em.merge(oldSongwriterIdOfTrackListNewTrack);
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
                Integer id = songwriter.getId();
                if (findSongwriter(id) == null) {
                    throw new NonexistentEntityException("The songwriter with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Songwriter songwriter;
            try {
                songwriter = em.getReference(Songwriter.class, id);
                songwriter.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The songwriter with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Track> trackListOrphanCheck = songwriter.getTrackList();
            for (Track trackListOrphanCheckTrack : trackListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Songwriter (" + songwriter + ") cannot be destroyed since the Track " + trackListOrphanCheckTrack + " in its trackList field has a non-nullable songwriterId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(songwriter);
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

    public List<Songwriter> findSongwriterEntities() {
        return findSongwriterEntities(true, -1, -1);
    }

    public List<Songwriter> findSongwriterEntities(int maxResults, int firstResult) {
        return findSongwriterEntities(false, maxResults, firstResult);
    }

    private List<Songwriter> findSongwriterEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Songwriter.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Songwriter findSongwriter(Integer id) {
        return em.find(Songwriter.class, id);
    }

    public int getSongwriterCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Songwriter> rt = cq.from(Songwriter.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

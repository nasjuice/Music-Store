package persistence.controllers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import persistence.entities.Album;
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
import persistence.entities.CoverArt;
import persistence.entities.Track;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class CoverArtJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public CoverArtJpaController() {
    }

    public void create(CoverArt coverArt) throws RollbackFailureException, Exception {
        if (coverArt.getAlbumList() == null) {
            coverArt.setAlbumList(new ArrayList<Album>());
        }
        if (coverArt.getTrackList() == null) {
            coverArt.setTrackList(new ArrayList<Track>());
        }
        try {
            utx.begin();
            List<Album> attachedAlbumList = new ArrayList<Album>();
            for (Album albumListAlbumToAttach : coverArt.getAlbumList()) {
                albumListAlbumToAttach = em.getReference(albumListAlbumToAttach.getClass(), albumListAlbumToAttach.getId());
                attachedAlbumList.add(albumListAlbumToAttach);
            }
            coverArt.setAlbumList(attachedAlbumList);
            List<Track> attachedTrackList = new ArrayList<Track>();
            for (Track trackListTrackToAttach : coverArt.getTrackList()) {
                trackListTrackToAttach = em.getReference(trackListTrackToAttach.getClass(), trackListTrackToAttach.getId());
                attachedTrackList.add(trackListTrackToAttach);
            }
            coverArt.setTrackList(attachedTrackList);
            em.persist(coverArt);
            for (Album albumListAlbum : coverArt.getAlbumList()) {
                CoverArt oldCoverArtIdOfAlbumListAlbum = albumListAlbum.getCoverArtId();
                albumListAlbum.setCoverArtId(coverArt);
                albumListAlbum = em.merge(albumListAlbum);
                if (oldCoverArtIdOfAlbumListAlbum != null) {
                    oldCoverArtIdOfAlbumListAlbum.getAlbumList().remove(albumListAlbum);
                    oldCoverArtIdOfAlbumListAlbum = em.merge(oldCoverArtIdOfAlbumListAlbum);
                }
            }
            for (Track trackListTrack : coverArt.getTrackList()) {
                CoverArt oldCoverArtIdOfTrackListTrack = trackListTrack.getCoverArtId();
                trackListTrack.setCoverArtId(coverArt);
                trackListTrack = em.merge(trackListTrack);
                if (oldCoverArtIdOfTrackListTrack != null) {
                    oldCoverArtIdOfTrackListTrack.getTrackList().remove(trackListTrack);
                    oldCoverArtIdOfTrackListTrack = em.merge(oldCoverArtIdOfTrackListTrack);
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

    public void edit(CoverArt coverArt) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            CoverArt persistentCoverArt = em.find(CoverArt.class, coverArt.getId());
            List<Album> albumListOld = persistentCoverArt.getAlbumList();
            List<Album> albumListNew = coverArt.getAlbumList();
            List<Track> trackListOld = persistentCoverArt.getTrackList();
            List<Track> trackListNew = coverArt.getTrackList();
            List<String> illegalOrphanMessages = null;
            for (Album albumListOldAlbum : albumListOld) {
                if (!albumListNew.contains(albumListOldAlbum)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Album " + albumListOldAlbum + " since its coverArtId field is not nullable.");
                }
            }
            for (Track trackListOldTrack : trackListOld) {
                if (!trackListNew.contains(trackListOldTrack)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Track " + trackListOldTrack + " since its coverArtId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Album> attachedAlbumListNew = new ArrayList<Album>();
            for (Album albumListNewAlbumToAttach : albumListNew) {
                albumListNewAlbumToAttach = em.getReference(albumListNewAlbumToAttach.getClass(), albumListNewAlbumToAttach.getId());
                attachedAlbumListNew.add(albumListNewAlbumToAttach);
            }
            albumListNew = attachedAlbumListNew;
            coverArt.setAlbumList(albumListNew);
            List<Track> attachedTrackListNew = new ArrayList<Track>();
            for (Track trackListNewTrackToAttach : trackListNew) {
                trackListNewTrackToAttach = em.getReference(trackListNewTrackToAttach.getClass(), trackListNewTrackToAttach.getId());
                attachedTrackListNew.add(trackListNewTrackToAttach);
            }
            trackListNew = attachedTrackListNew;
            coverArt.setTrackList(trackListNew);
            coverArt = em.merge(coverArt);
            for (Album albumListNewAlbum : albumListNew) {
                if (!albumListOld.contains(albumListNewAlbum)) {
                    CoverArt oldCoverArtIdOfAlbumListNewAlbum = albumListNewAlbum.getCoverArtId();
                    albumListNewAlbum.setCoverArtId(coverArt);
                    albumListNewAlbum = em.merge(albumListNewAlbum);
                    if (oldCoverArtIdOfAlbumListNewAlbum != null && !oldCoverArtIdOfAlbumListNewAlbum.equals(coverArt)) {
                        oldCoverArtIdOfAlbumListNewAlbum.getAlbumList().remove(albumListNewAlbum);
                        oldCoverArtIdOfAlbumListNewAlbum = em.merge(oldCoverArtIdOfAlbumListNewAlbum);
                    }
                }
            }
            for (Track trackListNewTrack : trackListNew) {
                if (!trackListOld.contains(trackListNewTrack)) {
                    CoverArt oldCoverArtIdOfTrackListNewTrack = trackListNewTrack.getCoverArtId();
                    trackListNewTrack.setCoverArtId(coverArt);
                    trackListNewTrack = em.merge(trackListNewTrack);
                    if (oldCoverArtIdOfTrackListNewTrack != null && !oldCoverArtIdOfTrackListNewTrack.equals(coverArt)) {
                        oldCoverArtIdOfTrackListNewTrack.getTrackList().remove(trackListNewTrack);
                        oldCoverArtIdOfTrackListNewTrack = em.merge(oldCoverArtIdOfTrackListNewTrack);
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
                Integer id = coverArt.getId();
                if (findCoverArt(id) == null) {
                    throw new NonexistentEntityException("The coverArt with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            CoverArt coverArt;
            try {
                coverArt = em.getReference(CoverArt.class, id);
                coverArt.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The coverArt with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Album> albumListOrphanCheck = coverArt.getAlbumList();
            for (Album albumListOrphanCheckAlbum : albumListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This CoverArt (" + coverArt + ") cannot be destroyed since the Album " + albumListOrphanCheckAlbum + " in its albumList field has a non-nullable coverArtId field.");
            }
            List<Track> trackListOrphanCheck = coverArt.getTrackList();
            for (Track trackListOrphanCheckTrack : trackListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This CoverArt (" + coverArt + ") cannot be destroyed since the Track " + trackListOrphanCheckTrack + " in its trackList field has a non-nullable coverArtId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(coverArt);
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

    public List<CoverArt> findCoverArtEntities() {
        return findCoverArtEntities(true, -1, -1);
    }

    public List<CoverArt> findCoverArtEntities(int maxResults, int firstResult) {
        return findCoverArtEntities(false, maxResults, firstResult);
    }

    private List<CoverArt> findCoverArtEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(CoverArt.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public CoverArt findCoverArt(Integer id) {
        return em.find(CoverArt.class, id);
    }

    public int getCoverArtCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<CoverArt> rt = cq.from(CoverArt.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

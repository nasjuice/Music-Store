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
import persistence.entities.RecordingLabel;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class RecordingLabelJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public RecordingLabelJpaController() {
    }

    public void create(RecordingLabel recordingLabel) throws RollbackFailureException, Exception {
        if (recordingLabel.getAlbumList() == null) {
            recordingLabel.setAlbumList(new ArrayList<Album>());
        }
        try {
            utx.begin();
            List<Album> attachedAlbumList = new ArrayList<Album>();
            for (Album albumListAlbumToAttach : recordingLabel.getAlbumList()) {
                albumListAlbumToAttach = em.getReference(albumListAlbumToAttach.getClass(), albumListAlbumToAttach.getId());
                attachedAlbumList.add(albumListAlbumToAttach);
            }
            recordingLabel.setAlbumList(attachedAlbumList);
            em.persist(recordingLabel);
            for (Album albumListAlbum : recordingLabel.getAlbumList()) {
                RecordingLabel oldRecordingLabelIdOfAlbumListAlbum = albumListAlbum.getRecordingLabelId();
                albumListAlbum.setRecordingLabelId(recordingLabel);
                albumListAlbum = em.merge(albumListAlbum);
                if (oldRecordingLabelIdOfAlbumListAlbum != null) {
                    oldRecordingLabelIdOfAlbumListAlbum.getAlbumList().remove(albumListAlbum);
                    oldRecordingLabelIdOfAlbumListAlbum = em.merge(oldRecordingLabelIdOfAlbumListAlbum);
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

    public void edit(RecordingLabel recordingLabel) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            RecordingLabel persistentRecordingLabel = em.find(RecordingLabel.class, recordingLabel.getId());
            List<Album> albumListOld = persistentRecordingLabel.getAlbumList();
            List<Album> albumListNew = recordingLabel.getAlbumList();
            List<String> illegalOrphanMessages = null;
            for (Album albumListOldAlbum : albumListOld) {
                if (!albumListNew.contains(albumListOldAlbum)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Album " + albumListOldAlbum + " since its recordingLabelId field is not nullable.");
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
            recordingLabel.setAlbumList(albumListNew);
            recordingLabel = em.merge(recordingLabel);
            for (Album albumListNewAlbum : albumListNew) {
                if (!albumListOld.contains(albumListNewAlbum)) {
                    RecordingLabel oldRecordingLabelIdOfAlbumListNewAlbum = albumListNewAlbum.getRecordingLabelId();
                    albumListNewAlbum.setRecordingLabelId(recordingLabel);
                    albumListNewAlbum = em.merge(albumListNewAlbum);
                    if (oldRecordingLabelIdOfAlbumListNewAlbum != null && !oldRecordingLabelIdOfAlbumListNewAlbum.equals(recordingLabel)) {
                        oldRecordingLabelIdOfAlbumListNewAlbum.getAlbumList().remove(albumListNewAlbum);
                        oldRecordingLabelIdOfAlbumListNewAlbum = em.merge(oldRecordingLabelIdOfAlbumListNewAlbum);
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
                Integer id = recordingLabel.getId();
                if (findRecordingLabel(id) == null) {
                    throw new NonexistentEntityException("The recordingLabel with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            RecordingLabel recordingLabel;
            try {
                recordingLabel = em.getReference(RecordingLabel.class, id);
                recordingLabel.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The recordingLabel with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Album> albumListOrphanCheck = recordingLabel.getAlbumList();
            for (Album albumListOrphanCheckAlbum : albumListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This RecordingLabel (" + recordingLabel + ") cannot be destroyed since the Album " + albumListOrphanCheckAlbum + " in its albumList field has a non-nullable recordingLabelId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(recordingLabel);
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

    public List<RecordingLabel> findRecordingLabelEntities() {
        return findRecordingLabelEntities(true, -1, -1);
    }

    public List<RecordingLabel> findRecordingLabelEntities(int maxResults, int firstResult) {
        return findRecordingLabelEntities(false, maxResults, firstResult);
    }

    private List<RecordingLabel> findRecordingLabelEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(RecordingLabel.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public RecordingLabel findRecordingLabel(Integer id) {
        return em.find(RecordingLabel.class, id);
    }

    public int getRecordingLabelCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<RecordingLabel> rt = cq.from(RecordingLabel.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

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
import persistence.entities.Genre;
import persistence.entities.ShopUser;
import persistence.entities.Track;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class GenreJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public GenreJpaController() {
    }

    public void create(Genre genre) throws RollbackFailureException, Exception {
        if (genre.getAlbumList() == null) {
            genre.setAlbumList(new ArrayList<Album>());
        }
        if (genre.getShopUserList() == null) {
            genre.setShopUserList(new ArrayList<ShopUser>());
        }
        if (genre.getTrackList() == null) {
            genre.setTrackList(new ArrayList<Track>());
        }
        try {
            utx.begin();
            List<Album> attachedAlbumList = new ArrayList<Album>();
            for (Album albumListAlbumToAttach : genre.getAlbumList()) {
                albumListAlbumToAttach = em.getReference(albumListAlbumToAttach.getClass(), albumListAlbumToAttach.getId());
                attachedAlbumList.add(albumListAlbumToAttach);
            }
            genre.setAlbumList(attachedAlbumList);
            List<ShopUser> attachedShopUserList = new ArrayList<ShopUser>();
            for (ShopUser shopUserListShopUserToAttach : genre.getShopUserList()) {
                shopUserListShopUserToAttach = em.getReference(shopUserListShopUserToAttach.getClass(), shopUserListShopUserToAttach.getId());
                attachedShopUserList.add(shopUserListShopUserToAttach);
            }
            genre.setShopUserList(attachedShopUserList);
            List<Track> attachedTrackList = new ArrayList<Track>();
            for (Track trackListTrackToAttach : genre.getTrackList()) {
                trackListTrackToAttach = em.getReference(trackListTrackToAttach.getClass(), trackListTrackToAttach.getId());
                attachedTrackList.add(trackListTrackToAttach);
            }
            genre.setTrackList(attachedTrackList);
            em.persist(genre);
            for (Album albumListAlbum : genre.getAlbumList()) {
                Genre oldGenreIdOfAlbumListAlbum = albumListAlbum.getGenreId();
                albumListAlbum.setGenreId(genre);
                albumListAlbum = em.merge(albumListAlbum);
                if (oldGenreIdOfAlbumListAlbum != null) {
                    oldGenreIdOfAlbumListAlbum.getAlbumList().remove(albumListAlbum);
                    oldGenreIdOfAlbumListAlbum = em.merge(oldGenreIdOfAlbumListAlbum);
                }
            }
            for (ShopUser shopUserListShopUser : genre.getShopUserList()) {
                Genre oldLastGenreSearchedOfShopUserListShopUser = shopUserListShopUser.getLastGenreSearched();
                shopUserListShopUser.setLastGenreSearched(genre);
                shopUserListShopUser = em.merge(shopUserListShopUser);
                if (oldLastGenreSearchedOfShopUserListShopUser != null) {
                    oldLastGenreSearchedOfShopUserListShopUser.getShopUserList().remove(shopUserListShopUser);
                    oldLastGenreSearchedOfShopUserListShopUser = em.merge(oldLastGenreSearchedOfShopUserListShopUser);
                }
            }
            for (Track trackListTrack : genre.getTrackList()) {
                Genre oldGenreIdOfTrackListTrack = trackListTrack.getGenreId();
                trackListTrack.setGenreId(genre);
                trackListTrack = em.merge(trackListTrack);
                if (oldGenreIdOfTrackListTrack != null) {
                    oldGenreIdOfTrackListTrack.getTrackList().remove(trackListTrack);
                    oldGenreIdOfTrackListTrack = em.merge(oldGenreIdOfTrackListTrack);
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

    public void edit(Genre genre) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Genre persistentGenre = em.find(Genre.class, genre.getId());
            List<Album> albumListOld = persistentGenre.getAlbumList();
            List<Album> albumListNew = genre.getAlbumList();
            List<ShopUser> shopUserListOld = persistentGenre.getShopUserList();
            List<ShopUser> shopUserListNew = genre.getShopUserList();
            List<Track> trackListOld = persistentGenre.getTrackList();
            List<Track> trackListNew = genre.getTrackList();
            List<String> illegalOrphanMessages = null;
            for (Album albumListOldAlbum : albumListOld) {
                if (!albumListNew.contains(albumListOldAlbum)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Album " + albumListOldAlbum + " since its genreId field is not nullable.");
                }
            }
            for (Track trackListOldTrack : trackListOld) {
                if (!trackListNew.contains(trackListOldTrack)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Track " + trackListOldTrack + " since its genreId field is not nullable.");
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
            genre.setAlbumList(albumListNew);
            List<ShopUser> attachedShopUserListNew = new ArrayList<ShopUser>();
            for (ShopUser shopUserListNewShopUserToAttach : shopUserListNew) {
                shopUserListNewShopUserToAttach = em.getReference(shopUserListNewShopUserToAttach.getClass(), shopUserListNewShopUserToAttach.getId());
                attachedShopUserListNew.add(shopUserListNewShopUserToAttach);
            }
            shopUserListNew = attachedShopUserListNew;
            genre.setShopUserList(shopUserListNew);
            List<Track> attachedTrackListNew = new ArrayList<Track>();
            for (Track trackListNewTrackToAttach : trackListNew) {
                trackListNewTrackToAttach = em.getReference(trackListNewTrackToAttach.getClass(), trackListNewTrackToAttach.getId());
                attachedTrackListNew.add(trackListNewTrackToAttach);
            }
            trackListNew = attachedTrackListNew;
            genre.setTrackList(trackListNew);
            genre = em.merge(genre);
            for (Album albumListNewAlbum : albumListNew) {
                if (!albumListOld.contains(albumListNewAlbum)) {
                    Genre oldGenreIdOfAlbumListNewAlbum = albumListNewAlbum.getGenreId();
                    albumListNewAlbum.setGenreId(genre);
                    albumListNewAlbum = em.merge(albumListNewAlbum);
                    if (oldGenreIdOfAlbumListNewAlbum != null && !oldGenreIdOfAlbumListNewAlbum.equals(genre)) {
                        oldGenreIdOfAlbumListNewAlbum.getAlbumList().remove(albumListNewAlbum);
                        oldGenreIdOfAlbumListNewAlbum = em.merge(oldGenreIdOfAlbumListNewAlbum);
                    }
                }
            }
            for (ShopUser shopUserListOldShopUser : shopUserListOld) {
                if (!shopUserListNew.contains(shopUserListOldShopUser)) {
                    shopUserListOldShopUser.setLastGenreSearched(null);
                    shopUserListOldShopUser = em.merge(shopUserListOldShopUser);
                }
            }
            for (ShopUser shopUserListNewShopUser : shopUserListNew) {
                if (!shopUserListOld.contains(shopUserListNewShopUser)) {
                    Genre oldLastGenreSearchedOfShopUserListNewShopUser = shopUserListNewShopUser.getLastGenreSearched();
                    shopUserListNewShopUser.setLastGenreSearched(genre);
                    shopUserListNewShopUser = em.merge(shopUserListNewShopUser);
                    if (oldLastGenreSearchedOfShopUserListNewShopUser != null && !oldLastGenreSearchedOfShopUserListNewShopUser.equals(genre)) {
                        oldLastGenreSearchedOfShopUserListNewShopUser.getShopUserList().remove(shopUserListNewShopUser);
                        oldLastGenreSearchedOfShopUserListNewShopUser = em.merge(oldLastGenreSearchedOfShopUserListNewShopUser);
                    }
                }
            }
            for (Track trackListNewTrack : trackListNew) {
                if (!trackListOld.contains(trackListNewTrack)) {
                    Genre oldGenreIdOfTrackListNewTrack = trackListNewTrack.getGenreId();
                    trackListNewTrack.setGenreId(genre);
                    trackListNewTrack = em.merge(trackListNewTrack);
                    if (oldGenreIdOfTrackListNewTrack != null && !oldGenreIdOfTrackListNewTrack.equals(genre)) {
                        oldGenreIdOfTrackListNewTrack.getTrackList().remove(trackListNewTrack);
                        oldGenreIdOfTrackListNewTrack = em.merge(oldGenreIdOfTrackListNewTrack);
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
                Integer id = genre.getId();
                if (findGenre(id) == null) {
                    throw new NonexistentEntityException("The genre with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Genre genre;
            try {
                genre = em.getReference(Genre.class, id);
                genre.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The genre with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Album> albumListOrphanCheck = genre.getAlbumList();
            for (Album albumListOrphanCheckAlbum : albumListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Genre (" + genre + ") cannot be destroyed since the Album " + albumListOrphanCheckAlbum + " in its albumList field has a non-nullable genreId field.");
            }
            List<Track> trackListOrphanCheck = genre.getTrackList();
            for (Track trackListOrphanCheckTrack : trackListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Genre (" + genre + ") cannot be destroyed since the Track " + trackListOrphanCheckTrack + " in its trackList field has a non-nullable genreId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<ShopUser> shopUserList = genre.getShopUserList();
            for (ShopUser shopUserListShopUser : shopUserList) {
                shopUserListShopUser.setLastGenreSearched(null);
                shopUserListShopUser = em.merge(shopUserListShopUser);
            }
            em.remove(genre);
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

    public List<Genre> findGenreEntities() {
        return findGenreEntities(true, -1, -1);
    }

    public List<Genre> findGenreEntities(int maxResults, int firstResult) {
        return findGenreEntities(false, maxResults, firstResult);
    }

    private List<Genre> findGenreEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Genre.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Genre findGenre(Integer id) {
        return em.find(Genre.class, id);
    }

    public int getGenreCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Genre> rt = cq.from(Genre.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

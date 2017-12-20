package persistence.controllers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import persistence.entities.Province;
import persistence.entities.Genre;
import persistence.entities.Review;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import persistence.controllers.exceptions.IllegalOrphanException;
import persistence.controllers.exceptions.NonexistentEntityException;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Invoice;
import persistence.entities.ShopUser;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class ShopUserJpaController implements Serializable {
    private static final Logger LOG = Logger.getLogger("ShopUserJpaController.class");
    
    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public ShopUserJpaController() {
    }

    public void create(ShopUser shopUser) throws RollbackFailureException, Exception {
        if (shopUser.getReviewList() == null) {
            shopUser.setReviewList(new ArrayList<Review>());
        }
        if (shopUser.getInvoiceList() == null) {
            shopUser.setInvoiceList(new ArrayList<Invoice>());
        }
        try {
            utx.begin();
            Province provinceId = shopUser.getProvinceId();
            if (provinceId != null) {
                provinceId = em.getReference(provinceId.getClass(), provinceId.getId());
                shopUser.setProvinceId(provinceId);
            }
            Genre lastGenreSearched = shopUser.getLastGenreSearched();
            if (lastGenreSearched != null) {
                lastGenreSearched = em.getReference(lastGenreSearched.getClass(), lastGenreSearched.getId());
                shopUser.setLastGenreSearched(lastGenreSearched);
            }
            List<Review> attachedReviewList = new ArrayList<Review>();
            for (Review reviewListReviewToAttach : shopUser.getReviewList()) {
                reviewListReviewToAttach = em.getReference(reviewListReviewToAttach.getClass(), reviewListReviewToAttach.getId());
                attachedReviewList.add(reviewListReviewToAttach);
            }
            shopUser.setReviewList(attachedReviewList);
            List<Invoice> attachedInvoiceList = new ArrayList<Invoice>();
            for (Invoice invoiceListInvoiceToAttach : shopUser.getInvoiceList()) {
                invoiceListInvoiceToAttach = em.getReference(invoiceListInvoiceToAttach.getClass(), invoiceListInvoiceToAttach.getId());
                attachedInvoiceList.add(invoiceListInvoiceToAttach);
            }
            shopUser.setInvoiceList(attachedInvoiceList);
            em.persist(shopUser);
            if (provinceId != null) {
                provinceId.getShopUserList().add(shopUser);
                provinceId = em.merge(provinceId);
            }
            if (lastGenreSearched != null) {
                lastGenreSearched.getShopUserList().add(shopUser);
                lastGenreSearched = em.merge(lastGenreSearched);
            }
            for (Review reviewListReview : shopUser.getReviewList()) {
                ShopUser oldUserIdOfReviewListReview = reviewListReview.getUserId();
                reviewListReview.setUserId(shopUser);
                reviewListReview = em.merge(reviewListReview);
                if (oldUserIdOfReviewListReview != null) {
                    oldUserIdOfReviewListReview.getReviewList().remove(reviewListReview);
                    oldUserIdOfReviewListReview = em.merge(oldUserIdOfReviewListReview);
                }
            }
            for (Invoice invoiceListInvoice : shopUser.getInvoiceList()) {
                ShopUser oldUserIdOfInvoiceListInvoice = invoiceListInvoice.getUserId();
                invoiceListInvoice.setUserId(shopUser);
                invoiceListInvoice = em.merge(invoiceListInvoice);
                if (oldUserIdOfInvoiceListInvoice != null) {
                    oldUserIdOfInvoiceListInvoice.getInvoiceList().remove(invoiceListInvoice);
                    oldUserIdOfInvoiceListInvoice = em.merge(oldUserIdOfInvoiceListInvoice);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            LOG.info(ex.getMessage());
            try {
                if (utx.getStatus() == Status.STATUS_ACTIVE)
                {
                    utx.rollback();
                }                
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        }
    }

    public void edit(ShopUser shopUser) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            ShopUser persistentShopUser = em.find(ShopUser.class, shopUser.getId());
            Province provinceIdOld = persistentShopUser.getProvinceId();
            Province provinceIdNew = shopUser.getProvinceId();
            Genre lastGenreSearchedOld = persistentShopUser.getLastGenreSearched();
            Genre lastGenreSearchedNew = shopUser.getLastGenreSearched();
            List<Review> reviewListOld = persistentShopUser.getReviewList();
            List<Review> reviewListNew = shopUser.getReviewList();
            List<Invoice> invoiceListOld = persistentShopUser.getInvoiceList();
            List<Invoice> invoiceListNew = shopUser.getInvoiceList();
            List<String> illegalOrphanMessages = null;
            for (Review reviewListOldReview : reviewListOld) {
                if (!reviewListNew.contains(reviewListOldReview)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Review " + reviewListOldReview + " since its userId field is not nullable.");
                }
            }
            for (Invoice invoiceListOldInvoice : invoiceListOld) {
                if (!invoiceListNew.contains(invoiceListOldInvoice)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Invoice " + invoiceListOldInvoice + " since its userId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (provinceIdNew != null) {
                provinceIdNew = em.getReference(provinceIdNew.getClass(), provinceIdNew.getId());
                shopUser.setProvinceId(provinceIdNew);
            }
            if (lastGenreSearchedNew != null) {
                lastGenreSearchedNew = em.getReference(lastGenreSearchedNew.getClass(), lastGenreSearchedNew.getId());
                shopUser.setLastGenreSearched(lastGenreSearchedNew);
            }
            List<Review> attachedReviewListNew = new ArrayList<Review>();
            for (Review reviewListNewReviewToAttach : reviewListNew) {
                reviewListNewReviewToAttach = em.getReference(reviewListNewReviewToAttach.getClass(), reviewListNewReviewToAttach.getId());
                attachedReviewListNew.add(reviewListNewReviewToAttach);
            }
            reviewListNew = attachedReviewListNew;
            shopUser.setReviewList(reviewListNew);
            List<Invoice> attachedInvoiceListNew = new ArrayList<Invoice>();
            for (Invoice invoiceListNewInvoiceToAttach : invoiceListNew) {
                invoiceListNewInvoiceToAttach = em.getReference(invoiceListNewInvoiceToAttach.getClass(), invoiceListNewInvoiceToAttach.getId());
                attachedInvoiceListNew.add(invoiceListNewInvoiceToAttach);
            }
            invoiceListNew = attachedInvoiceListNew;
            shopUser.setInvoiceList(invoiceListNew);
            shopUser = em.merge(shopUser);
            if (provinceIdOld != null && !provinceIdOld.equals(provinceIdNew)) {
                provinceIdOld.getShopUserList().remove(shopUser);
                provinceIdOld = em.merge(provinceIdOld);
            }
            if (provinceIdNew != null && !provinceIdNew.equals(provinceIdOld)) {
                provinceIdNew.getShopUserList().add(shopUser);
                provinceIdNew = em.merge(provinceIdNew);
            }
            if (lastGenreSearchedOld != null && !lastGenreSearchedOld.equals(lastGenreSearchedNew)) {
                lastGenreSearchedOld.getShopUserList().remove(shopUser);
                lastGenreSearchedOld = em.merge(lastGenreSearchedOld);
            }
            if (lastGenreSearchedNew != null && !lastGenreSearchedNew.equals(lastGenreSearchedOld)) {
                lastGenreSearchedNew.getShopUserList().add(shopUser);
                lastGenreSearchedNew = em.merge(lastGenreSearchedNew);
            }
            for (Review reviewListNewReview : reviewListNew) {
                if (!reviewListOld.contains(reviewListNewReview)) {
                    ShopUser oldUserIdOfReviewListNewReview = reviewListNewReview.getUserId();
                    reviewListNewReview.setUserId(shopUser);
                    reviewListNewReview = em.merge(reviewListNewReview);
                    if (oldUserIdOfReviewListNewReview != null && !oldUserIdOfReviewListNewReview.equals(shopUser)) {
                        oldUserIdOfReviewListNewReview.getReviewList().remove(reviewListNewReview);
                        oldUserIdOfReviewListNewReview = em.merge(oldUserIdOfReviewListNewReview);
                    }
                }
            }
            for (Invoice invoiceListNewInvoice : invoiceListNew) {
                if (!invoiceListOld.contains(invoiceListNewInvoice)) {
                    ShopUser oldUserIdOfInvoiceListNewInvoice = invoiceListNewInvoice.getUserId();
                    invoiceListNewInvoice.setUserId(shopUser);
                    invoiceListNewInvoice = em.merge(invoiceListNewInvoice);
                    if (oldUserIdOfInvoiceListNewInvoice != null && !oldUserIdOfInvoiceListNewInvoice.equals(shopUser)) {
                        oldUserIdOfInvoiceListNewInvoice.getInvoiceList().remove(invoiceListNewInvoice);
                        oldUserIdOfInvoiceListNewInvoice = em.merge(oldUserIdOfInvoiceListNewInvoice);
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
                Integer id = shopUser.getId();
                if (findShopUser(id) == null) {
                    throw new NonexistentEntityException("The shopUser with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            ShopUser shopUser;
            try {
                shopUser = em.getReference(ShopUser.class, id);
                shopUser.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The shopUser with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Review> reviewListOrphanCheck = shopUser.getReviewList();
            for (Review reviewListOrphanCheckReview : reviewListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This ShopUser (" + shopUser + ") cannot be destroyed since the Review " + reviewListOrphanCheckReview + " in its reviewList field has a non-nullable userId field.");
            }
            List<Invoice> invoiceListOrphanCheck = shopUser.getInvoiceList();
            for (Invoice invoiceListOrphanCheckInvoice : invoiceListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This ShopUser (" + shopUser + ") cannot be destroyed since the Invoice " + invoiceListOrphanCheckInvoice + " in its invoiceList field has a non-nullable userId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Province provinceId = shopUser.getProvinceId();
            if (provinceId != null) {
                provinceId.getShopUserList().remove(shopUser);
                provinceId = em.merge(provinceId);
            }
            Genre lastGenreSearched = shopUser.getLastGenreSearched();
            if (lastGenreSearched != null) {
                lastGenreSearched.getShopUserList().remove(shopUser);
                lastGenreSearched = em.merge(lastGenreSearched);
            }
            em.remove(shopUser);
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

    public List<ShopUser> findShopUserEntities() {
        return findShopUserEntities(true, -1, -1);
    }

    public List<ShopUser> findShopUserEntities(int maxResults, int firstResult) {
        return findShopUserEntities(false, maxResults, firstResult);
    }

    private List<ShopUser> findShopUserEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(ShopUser.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public ShopUser findShopUser(Integer id) {
        return em.find(ShopUser.class, id);
    }

    public int getShopUserCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<ShopUser> rt = cq.from(ShopUser.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    public boolean checkIfUserExists(ShopUser u){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        CriteriaQuery<ShopUser> cq = cb.createQuery(ShopUser.class);
        Root<ShopUser> r = cq.from(ShopUser.class);
        cq.select(r);
        cq.where(cb.equal(r, u));
        
        try{
            ShopUser user = em.createQuery(cq).getSingleResult();
            return true;
        }catch(NoResultException  e){
            return false;
        }
        catch (NonUniqueResultException e){
            // Should not get here but just in case.
            return true;
        }
        
    }
    
}

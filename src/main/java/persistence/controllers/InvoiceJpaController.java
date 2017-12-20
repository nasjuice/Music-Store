package persistence.controllers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import persistence.entities.ShopUser;
import persistence.entities.InvoiceAlbum;
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
import persistence.entities.Invoice;
import persistence.entities.InvoiceTrack;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class InvoiceJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public InvoiceJpaController() {
    }

    public void create(Invoice invoice) throws RollbackFailureException, Exception {
        if (invoice.getInvoiceAlbumList() == null) {
            invoice.setInvoiceAlbumList(new ArrayList<InvoiceAlbum>());
        }
        if (invoice.getInvoiceTrackList() == null) {
            invoice.setInvoiceTrackList(new ArrayList<InvoiceTrack>());
        }
        try {
            utx.begin();
            ShopUser userId = invoice.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                invoice.setUserId(userId);
            }
            List<InvoiceAlbum> attachedInvoiceAlbumList = new ArrayList<InvoiceAlbum>();
            for (InvoiceAlbum invoiceAlbumListInvoiceAlbumToAttach : invoice.getInvoiceAlbumList()) {
                invoiceAlbumListInvoiceAlbumToAttach = em.getReference(invoiceAlbumListInvoiceAlbumToAttach.getClass(), invoiceAlbumListInvoiceAlbumToAttach.getInvoiceAlbumPK());
                attachedInvoiceAlbumList.add(invoiceAlbumListInvoiceAlbumToAttach);
            }
            invoice.setInvoiceAlbumList(attachedInvoiceAlbumList);
            List<InvoiceTrack> attachedInvoiceTrackList = new ArrayList<InvoiceTrack>();
            for (InvoiceTrack invoiceTrackListInvoiceTrackToAttach : invoice.getInvoiceTrackList()) {
                invoiceTrackListInvoiceTrackToAttach = em.getReference(invoiceTrackListInvoiceTrackToAttach.getClass(), invoiceTrackListInvoiceTrackToAttach.getInvoiceTrackPK());
                attachedInvoiceTrackList.add(invoiceTrackListInvoiceTrackToAttach);
            }
            invoice.setInvoiceTrackList(attachedInvoiceTrackList);
            em.persist(invoice);
            if (userId != null) {
                userId.getInvoiceList().add(invoice);
                userId = em.merge(userId);
            }
            for (InvoiceAlbum invoiceAlbumListInvoiceAlbum : invoice.getInvoiceAlbumList()) {
                Invoice oldInvoiceOfInvoiceAlbumListInvoiceAlbum = invoiceAlbumListInvoiceAlbum.getInvoice();
                invoiceAlbumListInvoiceAlbum.setInvoice(invoice);
                invoiceAlbumListInvoiceAlbum = em.merge(invoiceAlbumListInvoiceAlbum);
                if (oldInvoiceOfInvoiceAlbumListInvoiceAlbum != null) {
                    oldInvoiceOfInvoiceAlbumListInvoiceAlbum.getInvoiceAlbumList().remove(invoiceAlbumListInvoiceAlbum);
                    oldInvoiceOfInvoiceAlbumListInvoiceAlbum = em.merge(oldInvoiceOfInvoiceAlbumListInvoiceAlbum);
                }
            }
            for (InvoiceTrack invoiceTrackListInvoiceTrack : invoice.getInvoiceTrackList()) {
                Invoice oldInvoiceOfInvoiceTrackListInvoiceTrack = invoiceTrackListInvoiceTrack.getInvoice();
                invoiceTrackListInvoiceTrack.setInvoice(invoice);
                invoiceTrackListInvoiceTrack = em.merge(invoiceTrackListInvoiceTrack);
                if (oldInvoiceOfInvoiceTrackListInvoiceTrack != null) {
                    oldInvoiceOfInvoiceTrackListInvoiceTrack.getInvoiceTrackList().remove(invoiceTrackListInvoiceTrack);
                    oldInvoiceOfInvoiceTrackListInvoiceTrack = em.merge(oldInvoiceOfInvoiceTrackListInvoiceTrack);
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

    public void edit(Invoice invoice) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Invoice persistentInvoice = em.find(Invoice.class, invoice.getId());
            ShopUser userIdOld = persistentInvoice.getUserId();
            ShopUser userIdNew = invoice.getUserId();
            List<InvoiceAlbum> invoiceAlbumListOld = persistentInvoice.getInvoiceAlbumList();
            List<InvoiceAlbum> invoiceAlbumListNew = invoice.getInvoiceAlbumList();
            List<InvoiceTrack> invoiceTrackListOld = persistentInvoice.getInvoiceTrackList();
            List<InvoiceTrack> invoiceTrackListNew = invoice.getInvoiceTrackList();
            List<String> illegalOrphanMessages = null;
            for (InvoiceAlbum invoiceAlbumListOldInvoiceAlbum : invoiceAlbumListOld) {
                if (!invoiceAlbumListNew.contains(invoiceAlbumListOldInvoiceAlbum)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain InvoiceAlbum " + invoiceAlbumListOldInvoiceAlbum + " since its invoice field is not nullable.");
                }
            }
            for (InvoiceTrack invoiceTrackListOldInvoiceTrack : invoiceTrackListOld) {
                if (!invoiceTrackListNew.contains(invoiceTrackListOldInvoiceTrack)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain InvoiceTrack " + invoiceTrackListOldInvoiceTrack + " since its invoice field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                invoice.setUserId(userIdNew);
            }
            List<InvoiceAlbum> attachedInvoiceAlbumListNew = new ArrayList<InvoiceAlbum>();
            for (InvoiceAlbum invoiceAlbumListNewInvoiceAlbumToAttach : invoiceAlbumListNew) {
                invoiceAlbumListNewInvoiceAlbumToAttach = em.getReference(invoiceAlbumListNewInvoiceAlbumToAttach.getClass(), invoiceAlbumListNewInvoiceAlbumToAttach.getInvoiceAlbumPK());
                attachedInvoiceAlbumListNew.add(invoiceAlbumListNewInvoiceAlbumToAttach);
            }
            invoiceAlbumListNew = attachedInvoiceAlbumListNew;
            invoice.setInvoiceAlbumList(invoiceAlbumListNew);
            List<InvoiceTrack> attachedInvoiceTrackListNew = new ArrayList<InvoiceTrack>();
            for (InvoiceTrack invoiceTrackListNewInvoiceTrackToAttach : invoiceTrackListNew) {
                invoiceTrackListNewInvoiceTrackToAttach = em.getReference(invoiceTrackListNewInvoiceTrackToAttach.getClass(), invoiceTrackListNewInvoiceTrackToAttach.getInvoiceTrackPK());
                attachedInvoiceTrackListNew.add(invoiceTrackListNewInvoiceTrackToAttach);
            }
            invoiceTrackListNew = attachedInvoiceTrackListNew;
            invoice.setInvoiceTrackList(invoiceTrackListNew);
            invoice = em.merge(invoice);
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getInvoiceList().remove(invoice);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getInvoiceList().add(invoice);
                userIdNew = em.merge(userIdNew);
            }
            for (InvoiceAlbum invoiceAlbumListNewInvoiceAlbum : invoiceAlbumListNew) {
                if (!invoiceAlbumListOld.contains(invoiceAlbumListNewInvoiceAlbum)) {
                    Invoice oldInvoiceOfInvoiceAlbumListNewInvoiceAlbum = invoiceAlbumListNewInvoiceAlbum.getInvoice();
                    invoiceAlbumListNewInvoiceAlbum.setInvoice(invoice);
                    invoiceAlbumListNewInvoiceAlbum = em.merge(invoiceAlbumListNewInvoiceAlbum);
                    if (oldInvoiceOfInvoiceAlbumListNewInvoiceAlbum != null && !oldInvoiceOfInvoiceAlbumListNewInvoiceAlbum.equals(invoice)) {
                        oldInvoiceOfInvoiceAlbumListNewInvoiceAlbum.getInvoiceAlbumList().remove(invoiceAlbumListNewInvoiceAlbum);
                        oldInvoiceOfInvoiceAlbumListNewInvoiceAlbum = em.merge(oldInvoiceOfInvoiceAlbumListNewInvoiceAlbum);
                    }
                }
            }
            for (InvoiceTrack invoiceTrackListNewInvoiceTrack : invoiceTrackListNew) {
                if (!invoiceTrackListOld.contains(invoiceTrackListNewInvoiceTrack)) {
                    Invoice oldInvoiceOfInvoiceTrackListNewInvoiceTrack = invoiceTrackListNewInvoiceTrack.getInvoice();
                    invoiceTrackListNewInvoiceTrack.setInvoice(invoice);
                    invoiceTrackListNewInvoiceTrack = em.merge(invoiceTrackListNewInvoiceTrack);
                    if (oldInvoiceOfInvoiceTrackListNewInvoiceTrack != null && !oldInvoiceOfInvoiceTrackListNewInvoiceTrack.equals(invoice)) {
                        oldInvoiceOfInvoiceTrackListNewInvoiceTrack.getInvoiceTrackList().remove(invoiceTrackListNewInvoiceTrack);
                        oldInvoiceOfInvoiceTrackListNewInvoiceTrack = em.merge(oldInvoiceOfInvoiceTrackListNewInvoiceTrack);
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
                Integer id = invoice.getId();
                if (findInvoice(id) == null) {
                    throw new NonexistentEntityException("The invoice with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Invoice invoice;
            try {
                invoice = em.getReference(Invoice.class, id);
                invoice.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The invoice with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<InvoiceAlbum> invoiceAlbumListOrphanCheck = invoice.getInvoiceAlbumList();
            for (InvoiceAlbum invoiceAlbumListOrphanCheckInvoiceAlbum : invoiceAlbumListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Invoice (" + invoice + ") cannot be destroyed since the InvoiceAlbum " + invoiceAlbumListOrphanCheckInvoiceAlbum + " in its invoiceAlbumList field has a non-nullable invoice field.");
            }
            List<InvoiceTrack> invoiceTrackListOrphanCheck = invoice.getInvoiceTrackList();
            for (InvoiceTrack invoiceTrackListOrphanCheckInvoiceTrack : invoiceTrackListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Invoice (" + invoice + ") cannot be destroyed since the InvoiceTrack " + invoiceTrackListOrphanCheckInvoiceTrack + " in its invoiceTrackList field has a non-nullable invoice field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            ShopUser userId = invoice.getUserId();
            if (userId != null) {
                userId.getInvoiceList().remove(invoice);
                userId = em.merge(userId);
            }
            em.remove(invoice);
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

    public List<Invoice> findInvoiceEntities() {
        return findInvoiceEntities(true, -1, -1);
    }

    public List<Invoice> findInvoiceEntities(int maxResults, int firstResult) {
        return findInvoiceEntities(false, maxResults, firstResult);
    }

    private List<Invoice> findInvoiceEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Invoice.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Invoice findInvoice(Integer id) {
        return em.find(Invoice.class, id);
    }

    public int getInvoiceCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Invoice> rt = cq.from(Invoice.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

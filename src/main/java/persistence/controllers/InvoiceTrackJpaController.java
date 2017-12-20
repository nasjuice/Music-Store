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
import persistence.controllers.exceptions.PreexistingEntityException;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Invoice;
import persistence.entities.InvoiceTrack;
import persistence.entities.InvoiceTrackPK;
import persistence.entities.Track;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class InvoiceTrackJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public InvoiceTrackJpaController() {
    }

    public void create(InvoiceTrack invoiceTrack) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (invoiceTrack.getInvoiceTrackPK() == null) {
            invoiceTrack.setInvoiceTrackPK(new InvoiceTrackPK());
        }
        invoiceTrack.getInvoiceTrackPK().setInvoiceId(invoiceTrack.getInvoice().getId());
        invoiceTrack.getInvoiceTrackPK().setTrackId(invoiceTrack.getTrack().getId());
        try {
            utx.begin();
            Invoice invoice = invoiceTrack.getInvoice();
            if (invoice != null) {
                invoice = em.getReference(invoice.getClass(), invoice.getId());
                invoiceTrack.setInvoice(invoice);
            }
            Track track = invoiceTrack.getTrack();
            if (track != null) {
                track = em.getReference(track.getClass(), track.getId());
                invoiceTrack.setTrack(track);
            }
            em.persist(invoiceTrack);
            if (invoice != null) {
                invoice.getInvoiceTrackList().add(invoiceTrack);
                invoice = em.merge(invoice);
            }
            if (track != null) {
                track.getInvoiceTrackList().add(invoiceTrack);
                track = em.merge(track);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findInvoiceTrack(invoiceTrack.getInvoiceTrackPK()) != null) {
                throw new PreexistingEntityException("InvoiceTrack " + invoiceTrack + " already exists.", ex);
            }
            throw ex;
        }
    }

    public void edit(InvoiceTrack invoiceTrack) throws NonexistentEntityException, RollbackFailureException, Exception {
        invoiceTrack.getInvoiceTrackPK().setInvoiceId(invoiceTrack.getInvoice().getId());
        invoiceTrack.getInvoiceTrackPK().setTrackId(invoiceTrack.getTrack().getId());
        try {
            utx.begin();
            InvoiceTrack persistentInvoiceTrack = em.find(InvoiceTrack.class, invoiceTrack.getInvoiceTrackPK());
            Invoice invoiceOld = persistentInvoiceTrack.getInvoice();
            Invoice invoiceNew = invoiceTrack.getInvoice();
            Track trackOld = persistentInvoiceTrack.getTrack();
            Track trackNew = invoiceTrack.getTrack();
            if (invoiceNew != null) {
                invoiceNew = em.getReference(invoiceNew.getClass(), invoiceNew.getId());
                invoiceTrack.setInvoice(invoiceNew);
            }
            if (trackNew != null) {
                trackNew = em.getReference(trackNew.getClass(), trackNew.getId());
                invoiceTrack.setTrack(trackNew);
            }
            invoiceTrack = em.merge(invoiceTrack);
            if (invoiceOld != null && !invoiceOld.equals(invoiceNew)) {
                invoiceOld.getInvoiceTrackList().remove(invoiceTrack);
                invoiceOld = em.merge(invoiceOld);
            }
            if (invoiceNew != null && !invoiceNew.equals(invoiceOld)) {
                invoiceNew.getInvoiceTrackList().add(invoiceTrack);
                invoiceNew = em.merge(invoiceNew);
            }
            if (trackOld != null && !trackOld.equals(trackNew)) {
                trackOld.getInvoiceTrackList().remove(invoiceTrack);
                trackOld = em.merge(trackOld);
            }
            if (trackNew != null && !trackNew.equals(trackOld)) {
                trackNew.getInvoiceTrackList().add(invoiceTrack);
                trackNew = em.merge(trackNew);
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
                InvoiceTrackPK id = invoiceTrack.getInvoiceTrackPK();
                if (findInvoiceTrack(id) == null) {
                    throw new NonexistentEntityException("The invoiceTrack with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(InvoiceTrackPK id) throws NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            InvoiceTrack invoiceTrack;
            try {
                invoiceTrack = em.getReference(InvoiceTrack.class, id);
                invoiceTrack.getInvoiceTrackPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The invoiceTrack with id " + id + " no longer exists.", enfe);
            }
            Invoice invoice = invoiceTrack.getInvoice();
            if (invoice != null) {
                invoice.getInvoiceTrackList().remove(invoiceTrack);
                invoice = em.merge(invoice);
            }
            Track track = invoiceTrack.getTrack();
            if (track != null) {
                track.getInvoiceTrackList().remove(invoiceTrack);
                track = em.merge(track);
            }
            em.remove(invoiceTrack);
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

    public List<InvoiceTrack> findInvoiceTrackEntities() {
        return findInvoiceTrackEntities(true, -1, -1);
    }

    public List<InvoiceTrack> findInvoiceTrackEntities(int maxResults, int firstResult) {
        return findInvoiceTrackEntities(false, maxResults, firstResult);
    }

    private List<InvoiceTrack> findInvoiceTrackEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(InvoiceTrack.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public InvoiceTrack findInvoiceTrack(InvoiceTrackPK id) {
        return em.find(InvoiceTrack.class, id);
    }

    public int getInvoiceTrackCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<InvoiceTrack> rt = cq.from(InvoiceTrack.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

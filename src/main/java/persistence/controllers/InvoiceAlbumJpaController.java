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
import persistence.entities.Album;
import persistence.entities.InvoiceAlbum;
import persistence.entities.InvoiceAlbumPK;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class InvoiceAlbumJpaController implements Serializable {

    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public InvoiceAlbumJpaController() {
    }

    public void create(InvoiceAlbum invoiceAlbum) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (invoiceAlbum.getInvoiceAlbumPK() == null) {
            invoiceAlbum.setInvoiceAlbumPK(new InvoiceAlbumPK());
        }
        invoiceAlbum.getInvoiceAlbumPK().setAlbumId(invoiceAlbum.getAlbum().getId());
        invoiceAlbum.getInvoiceAlbumPK().setInvoiceId(invoiceAlbum.getInvoice().getId());
        try {
            utx.begin();
            Invoice invoice = invoiceAlbum.getInvoice();
            if (invoice != null) {
                invoice = em.getReference(invoice.getClass(), invoice.getId());
                invoiceAlbum.setInvoice(invoice);
            }
            Album album = invoiceAlbum.getAlbum();
            if (album != null) {
                album = em.getReference(album.getClass(), album.getId());
                invoiceAlbum.setAlbum(album);
            }
            em.persist(invoiceAlbum);
            if (invoice != null) {
                invoice.getInvoiceAlbumList().add(invoiceAlbum);
                invoice = em.merge(invoice);
            }
            if (album != null) {
                album.getInvoiceAlbumList().add(invoiceAlbum);
                album = em.merge(album);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findInvoiceAlbum(invoiceAlbum.getInvoiceAlbumPK()) != null) {
                throw new PreexistingEntityException("InvoiceAlbum " + invoiceAlbum + " already exists.", ex);
            }
            throw ex;
        }
    }

    public void edit(InvoiceAlbum invoiceAlbum) throws NonexistentEntityException, RollbackFailureException, Exception {
        invoiceAlbum.getInvoiceAlbumPK().setAlbumId(invoiceAlbum.getAlbum().getId());
        invoiceAlbum.getInvoiceAlbumPK().setInvoiceId(invoiceAlbum.getInvoice().getId());
        try {
            utx.begin();
            InvoiceAlbum persistentInvoiceAlbum = em.find(InvoiceAlbum.class, invoiceAlbum.getInvoiceAlbumPK());
            Invoice invoiceOld = persistentInvoiceAlbum.getInvoice();
            Invoice invoiceNew = invoiceAlbum.getInvoice();
            Album albumOld = persistentInvoiceAlbum.getAlbum();
            Album albumNew = invoiceAlbum.getAlbum();
            if (invoiceNew != null) {
                invoiceNew = em.getReference(invoiceNew.getClass(), invoiceNew.getId());
                invoiceAlbum.setInvoice(invoiceNew);
            }
            if (albumNew != null) {
                albumNew = em.getReference(albumNew.getClass(), albumNew.getId());
                invoiceAlbum.setAlbum(albumNew);
            }
            invoiceAlbum = em.merge(invoiceAlbum);
            if (invoiceOld != null && !invoiceOld.equals(invoiceNew)) {
                invoiceOld.getInvoiceAlbumList().remove(invoiceAlbum);
                invoiceOld = em.merge(invoiceOld);
            }
            if (invoiceNew != null && !invoiceNew.equals(invoiceOld)) {
                invoiceNew.getInvoiceAlbumList().add(invoiceAlbum);
                invoiceNew = em.merge(invoiceNew);
            }
            if (albumOld != null && !albumOld.equals(albumNew)) {
                albumOld.getInvoiceAlbumList().remove(invoiceAlbum);
                albumOld = em.merge(albumOld);
            }
            if (albumNew != null && !albumNew.equals(albumOld)) {
                albumNew.getInvoiceAlbumList().add(invoiceAlbum);
                albumNew = em.merge(albumNew);
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
                InvoiceAlbumPK id = invoiceAlbum.getInvoiceAlbumPK();
                if (findInvoiceAlbum(id) == null) {
                    throw new NonexistentEntityException("The invoiceAlbum with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(InvoiceAlbumPK id) throws NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            InvoiceAlbum invoiceAlbum;
            try {
                invoiceAlbum = em.getReference(InvoiceAlbum.class, id);
                invoiceAlbum.getInvoiceAlbumPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The invoiceAlbum with id " + id + " no longer exists.", enfe);
            }
            Invoice invoice = invoiceAlbum.getInvoice();
            if (invoice != null) {
                invoice.getInvoiceAlbumList().remove(invoiceAlbum);
                invoice = em.merge(invoice);
            }
            Album album = invoiceAlbum.getAlbum();
            if (album != null) {
                album.getInvoiceAlbumList().remove(invoiceAlbum);
                album = em.merge(album);
            }
            em.remove(invoiceAlbum);
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

    public List<InvoiceAlbum> findInvoiceAlbumEntities() {
        return findInvoiceAlbumEntities(true, -1, -1);
    }

    public List<InvoiceAlbum> findInvoiceAlbumEntities(int maxResults, int firstResult) {
        return findInvoiceAlbumEntities(false, maxResults, firstResult);
    }

    private List<InvoiceAlbum> findInvoiceAlbumEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(InvoiceAlbum.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public InvoiceAlbum findInvoiceAlbum(InvoiceAlbumPK id) {
        return em.find(InvoiceAlbum.class, id);
    }

    public int getInvoiceAlbumCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<InvoiceAlbum> rt = cq.from(InvoiceAlbum.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}

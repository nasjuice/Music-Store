
package persistence.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Erika Bourque
 */
@Entity
@Table(name = "invoice_album")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "InvoiceAlbum.findAll", query = "SELECT i FROM InvoiceAlbum i")
    , @NamedQuery(name = "InvoiceAlbum.findByInvoiceId", query = "SELECT i FROM InvoiceAlbum i WHERE i.invoiceAlbumPK.invoiceId = :invoiceId")
    , @NamedQuery(name = "InvoiceAlbum.findByAlbumId", query = "SELECT i FROM InvoiceAlbum i WHERE i.invoiceAlbumPK.albumId = :albumId")
    , @NamedQuery(name = "InvoiceAlbum.findByFinalPrice", query = "SELECT i FROM InvoiceAlbum i WHERE i.finalPrice = :finalPrice")
    , @NamedQuery(name = "InvoiceAlbum.findByRemovalStatus", query = "SELECT i FROM InvoiceAlbum i WHERE i.removalStatus = :removalStatus")
    , @NamedQuery(name = "InvoiceAlbum.findByRemovalDate", query = "SELECT i FROM InvoiceAlbum i WHERE i.removalDate = :removalDate")})
public class InvoiceAlbum implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected InvoiceAlbumPK invoiceAlbumPK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "final_price")
    private double finalPrice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "removal_status")
    private short removalStatus;
    @Column(name = "removal_date")
    @Temporal(TemporalType.DATE)
    private Date removalDate;
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Invoice invoice;
    @JoinColumn(name = "album_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Album album;

    public InvoiceAlbum() {
    }

    public InvoiceAlbum(InvoiceAlbumPK invoiceAlbumPK) {
        this.invoiceAlbumPK = invoiceAlbumPK;
    }

    public InvoiceAlbum(InvoiceAlbumPK invoiceAlbumPK, double finalPrice, short removalStatus) {
        this.invoiceAlbumPK = invoiceAlbumPK;
        this.finalPrice = finalPrice;
        this.removalStatus = removalStatus;
    }

    public InvoiceAlbum(int invoiceId, int albumId) {
        this.invoiceAlbumPK = new InvoiceAlbumPK(invoiceId, albumId);
    }

    public InvoiceAlbumPK getInvoiceAlbumPK() {
        return invoiceAlbumPK;
    }

    public void setInvoiceAlbumPK(InvoiceAlbumPK invoiceAlbumPK) {
        this.invoiceAlbumPK = invoiceAlbumPK;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public short getRemovalStatus() {
        return removalStatus;
    }

    public void setRemovalStatus(short removalStatus) {
        this.removalStatus = removalStatus;
    }

    public Date getRemovalDate() {
        return removalDate;
    }

    public void setRemovalDate(Date removalDate) {
        this.removalDate = removalDate;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (invoiceAlbumPK != null ? invoiceAlbumPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof InvoiceAlbum)) {
            return false;
        }
        InvoiceAlbum other = (InvoiceAlbum) object;
        if ((this.invoiceAlbumPK == null && other.invoiceAlbumPK != null) || (this.invoiceAlbumPK != null && !this.invoiceAlbumPK.equals(other.invoiceAlbumPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "persistence.entities.InvoiceAlbum[ invoiceAlbumPK=" + invoiceAlbumPK + " ]";
    }
    
}

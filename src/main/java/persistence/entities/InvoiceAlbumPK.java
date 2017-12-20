
package persistence.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Erika Bourque
 */
@Embeddable
public class InvoiceAlbumPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "invoice_id")
    private int invoiceId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "album_id")
    private int albumId;

    public InvoiceAlbumPK() {
    }

    public InvoiceAlbumPK(int invoiceId, int albumId) {
        this.invoiceId = invoiceId;
        this.albumId = albumId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) invoiceId;
        hash += (int) albumId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof InvoiceAlbumPK)) {
            return false;
        }
        InvoiceAlbumPK other = (InvoiceAlbumPK) object;
        if (this.invoiceId != other.invoiceId) {
            return false;
        }
        if (this.albumId != other.albumId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "persistence.entities.InvoiceAlbumPK[ invoiceId=" + invoiceId + ", albumId=" + albumId + " ]";
    }
    
}

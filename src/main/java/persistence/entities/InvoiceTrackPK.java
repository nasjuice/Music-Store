
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
public class InvoiceTrackPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "invoice_id")
    private int invoiceId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "track_id")
    private int trackId;

    public InvoiceTrackPK() {
    }

    public InvoiceTrackPK(int invoiceId, int trackId) {
        this.invoiceId = invoiceId;
        this.trackId = trackId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) invoiceId;
        hash += (int) trackId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof InvoiceTrackPK)) {
            return false;
        }
        InvoiceTrackPK other = (InvoiceTrackPK) object;
        if (this.invoiceId != other.invoiceId) {
            return false;
        }
        if (this.trackId != other.trackId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "persistence.entities.InvoiceTrackPK[ invoiceId=" + invoiceId + ", trackId=" + trackId + " ]";
    }
    
}

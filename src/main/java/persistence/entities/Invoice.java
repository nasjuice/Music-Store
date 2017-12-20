
package persistence.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Erika Bourque
 */
@Entity
@Table(name = "invoice")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Invoice.findAll", query = "SELECT i FROM Invoice i")
    , @NamedQuery(name = "Invoice.findById", query = "SELECT i FROM Invoice i WHERE i.id = :id")
    , @NamedQuery(name = "Invoice.findBySaleDate", query = "SELECT i FROM Invoice i WHERE i.saleDate = :saleDate")
    , @NamedQuery(name = "Invoice.findByTotalNetValue", query = "SELECT i FROM Invoice i WHERE i.totalNetValue = :totalNetValue")
    , @NamedQuery(name = "Invoice.findByPstTax", query = "SELECT i FROM Invoice i WHERE i.pstTax = :pstTax")
    , @NamedQuery(name = "Invoice.findByGstTax", query = "SELECT i FROM Invoice i WHERE i.gstTax = :gstTax")
    , @NamedQuery(name = "Invoice.findByHstTax", query = "SELECT i FROM Invoice i WHERE i.hstTax = :hstTax")
    , @NamedQuery(name = "Invoice.findByTotalGrossValue", query = "SELECT i FROM Invoice i WHERE i.totalGrossValue = :totalGrossValue")
    , @NamedQuery(name = "Invoice.findByRemovalStatus", query = "SELECT i FROM Invoice i WHERE i.removalStatus = :removalStatus")
    , @NamedQuery(name = "Invoice.findByRemovalDate", query = "SELECT i FROM Invoice i WHERE i.removalDate = :removalDate")})
public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sale_date")
    @Temporal(TemporalType.DATE)
    private Date saleDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_net_value")
    private double totalNetValue;
    @Basic(optional = false)
    @NotNull
    @Column(name = "pst_tax")
    private double pstTax;
    @Basic(optional = false)
    @NotNull
    @Column(name = "gst_tax")
    private double gstTax;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hst_tax")
    private double hstTax;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_gross_value")
    private double totalGrossValue;
    @Basic(optional = false)
    @NotNull
    @Column(name = "removal_status")
    private short removalStatus;
    @Column(name = "removal_date")
    @Temporal(TemporalType.DATE)
    private Date removalDate;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "invoice")
    private List<InvoiceAlbum> invoiceAlbumList;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private ShopUser userId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "invoice")
    private List<InvoiceTrack> invoiceTrackList;

    public Invoice() {
    }

    public Invoice(Integer id) {
        this.id = id;
    }

    public Invoice(Integer id, Date saleDate, double totalNetValue, double pstTax, double gstTax, double hstTax, double totalGrossValue, short removalStatus) {
        this.id = id;
        this.saleDate = saleDate;
        this.totalNetValue = totalNetValue;
        this.pstTax = pstTax;
        this.gstTax = gstTax;
        this.hstTax = hstTax;
        this.totalGrossValue = totalGrossValue;
        this.removalStatus = removalStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Date saleDate) {
        this.saleDate = saleDate;
    }

    public double getTotalNetValue() {
        return totalNetValue;
    }

    public void setTotalNetValue(double totalNetValue) {
        this.totalNetValue = totalNetValue;
    }

    public double getPstTax() {
        return pstTax;
    }

    public void setPstTax(double pstTax) {
        this.pstTax = pstTax;
    }

    public double getGstTax() {
        return gstTax;
    }

    public void setGstTax(double gstTax) {
        this.gstTax = gstTax;
    }

    public double getHstTax() {
        return hstTax;
    }

    public void setHstTax(double hstTax) {
        this.hstTax = hstTax;
    }

    public double getTotalGrossValue() {
        return totalGrossValue;
    }

    public void setTotalGrossValue(double totalGrossValue) {
        this.totalGrossValue = totalGrossValue;
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

    @XmlTransient
    public List<InvoiceAlbum> getInvoiceAlbumList() {
        return invoiceAlbumList;
    }

    public void setInvoiceAlbumList(List<InvoiceAlbum> invoiceAlbumList) {
        this.invoiceAlbumList = invoiceAlbumList;
    }

    public ShopUser getUserId() {
        return userId;
    }

    public void setUserId(ShopUser userId) {
        this.userId = userId;
    }

    @XmlTransient
    public List<InvoiceTrack> getInvoiceTrackList() {
        return invoiceTrackList;
    }

    public void setInvoiceTrackList(List<InvoiceTrack> invoiceTrackList) {
        this.invoiceTrackList = invoiceTrackList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Invoice)) {
            return false;
        }
        Invoice other = (Invoice) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "persistence.entities.Invoice[ id=" + id + " ]";
    }
    
}

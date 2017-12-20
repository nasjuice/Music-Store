
package persistence.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Erika Bourque
 */
@Entity
@Table(name = "province")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Province.findAll", query = "SELECT p FROM Province p")
    , @NamedQuery(name = "Province.findById", query = "SELECT p FROM Province p WHERE p.id = :id")
    , @NamedQuery(name = "Province.findByName", query = "SELECT p FROM Province p WHERE p.name = :name")
    , @NamedQuery(name = "Province.findByPstRate", query = "SELECT p FROM Province p WHERE p.pstRate = :pstRate")
    , @NamedQuery(name = "Province.findByGstRate", query = "SELECT p FROM Province p WHERE p.gstRate = :gstRate")
    , @NamedQuery(name = "Province.findByHstRate", query = "SELECT p FROM Province p WHERE p.hstRate = :hstRate")})
public class Province implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Column(name = "pst_rate")
    private double pstRate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "gst_rate")
    private double gstRate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hst_rate")
    private double hstRate;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "provinceId")
    private List<ShopUser> shopUserList;

    public Province() {
    }

    public Province(Integer id) {
        this.id = id;
    }

    public Province(Integer id, String name, double pstRate, double gstRate, double hstRate) {
        this.id = id;
        this.name = name;
        this.pstRate = pstRate;
        this.gstRate = gstRate;
        this.hstRate = hstRate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPstRate() {
        return pstRate;
    }

    public void setPstRate(double pstRate) {
        this.pstRate = pstRate;
    }

    public double getGstRate() {
        return gstRate;
    }

    public void setGstRate(double gstRate) {
        this.gstRate = gstRate;
    }

    public double getHstRate() {
        return hstRate;
    }

    public void setHstRate(double hstRate) {
        this.hstRate = hstRate;
    }

    @XmlTransient
    public List<ShopUser> getShopUserList() {
        return shopUserList;
    }

    public void setShopUserList(List<ShopUser> shopUserList) {
        this.shopUserList = shopUserList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Province)) {
            return false;
        }
        Province other = (Province) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "" + id + "";
    }
    
}

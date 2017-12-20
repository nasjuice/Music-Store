
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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
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
@Table(name = "shop_user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ShopUser.findAll", query = "SELECT s FROM ShopUser s")
    , @NamedQuery(name = "ShopUser.findById", query = "SELECT s FROM ShopUser s WHERE s.id = :id")
    , @NamedQuery(name = "ShopUser.findByTitle", query = "SELECT s FROM ShopUser s WHERE s.title = :title")
    , @NamedQuery(name = "ShopUser.findByLastName", query = "SELECT s FROM ShopUser s WHERE s.lastName = :lastName")
    , @NamedQuery(name = "ShopUser.findByFirstName", query = "SELECT s FROM ShopUser s WHERE s.firstName = :firstName")
    , @NamedQuery(name = "ShopUser.findByCompanyName", query = "SELECT s FROM ShopUser s WHERE s.companyName = :companyName")
    , @NamedQuery(name = "ShopUser.findByStreetAddress", query = "SELECT s FROM ShopUser s WHERE s.streetAddress = :streetAddress")
    , @NamedQuery(name = "ShopUser.findByCity", query = "SELECT s FROM ShopUser s WHERE s.city = :city")
    , @NamedQuery(name = "ShopUser.findByCountry", query = "SELECT s FROM ShopUser s WHERE s.country = :country")
    , @NamedQuery(name = "ShopUser.findByPostalCode", query = "SELECT s FROM ShopUser s WHERE s.postalCode = :postalCode")
    , @NamedQuery(name = "ShopUser.findByHomePhone", query = "SELECT s FROM ShopUser s WHERE s.homePhone = :homePhone")
    , @NamedQuery(name = "ShopUser.findByCellPhone", query = "SELECT s FROM ShopUser s WHERE s.cellPhone = :cellPhone")
    , @NamedQuery(name = "ShopUser.findByEmail", query = "SELECT s FROM ShopUser s WHERE s.email = :email")
    , @NamedQuery(name = "ShopUser.findBySalt", query = "SELECT s FROM ShopUser s WHERE s.salt = :salt")
    , @NamedQuery(name = "ShopUser.findByIsManager", query = "SELECT s FROM ShopUser s WHERE s.isManager = :isManager")})
public class ShopUser implements Serializable {

    @Size(max = 255)
    @Column(name = "street_address_2")
    private String streetAddress2;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "hashed_pw")
    private byte[] hashedPw;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "title")
    private String title;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "last_name")
    private String lastName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "first_name")
    private String firstName;
    @Size(max = 255)
    @Column(name = "company_name")
    private String companyName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "street_address")
    private String streetAddress;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "city")
    private String city;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "country")
    private String country;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "postal_code")
    private String postalCode;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 15)
    @Column(name = "home_phone")
    private String homePhone;
    @Size(max = 15)
    @Column(name = "cell_phone")
    private String cellPhone;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "email")
    private String email;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "salt")
    private String salt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "is_manager")
    private short isManager;
    @JoinColumn(name = "province_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Province provinceId;
    @JoinColumn(name = "last_genre_searched", referencedColumnName = "id")
    @ManyToOne
    private Genre lastGenreSearched;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private List<Review> reviewList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private List<Invoice> invoiceList;

    public ShopUser() {
    }

    public ShopUser(Integer id) {
        this.id = id;
    }

    public ShopUser(Integer id, String title, String lastName, String firstName, String streetAddress, String city, String country, String postalCode, String homePhone, String email, byte[] hashedPw, String salt, short isManager) {
        this.id = id;
        this.title = title;
        this.lastName = lastName;
        this.firstName = firstName;
        this.streetAddress = streetAddress;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.homePhone = homePhone;
        this.email = email;
        this.hashedPw = hashedPw;
        this.salt = salt;
        this.isManager = isManager;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getHashedPw() {
        return hashedPw;
    }

    public void setHashedPw(byte[] hashedPw) {
        this.hashedPw = hashedPw;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public short getIsManager() {
        return isManager;
    }

    public void setIsManager(short isManager) {
        this.isManager = isManager;
    }

    public Province getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Province provinceId) {
        this.provinceId = provinceId;
    }

    public Genre getLastGenreSearched() {
        return lastGenreSearched;
    }

    public void setLastGenreSearched(Genre lastGenreSearched) {
        this.lastGenreSearched = lastGenreSearched;
    }

    @XmlTransient
    public List<Review> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @XmlTransient
    public List<Invoice> getInvoiceList() {
        return invoiceList;
    }

    public void setInvoiceList(List<Invoice> invoiceList) {
        this.invoiceList = invoiceList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ShopUser)) {
            return false;
        }
        ShopUser other = (ShopUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "" + id + "";
    }

    public String getStreetAddress2() {
        return streetAddress2;
    }

    public void setStreetAddress2(String streetAddress2) {
        this.streetAddress2 = streetAddress2;
    }    
}


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
@Table(name = "newsfeed")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Newsfeed.findAll", query = "SELECT n FROM Newsfeed n")
    , @NamedQuery(name = "Newsfeed.findById", query = "SELECT n FROM Newsfeed n WHERE n.id = :id")
    , @NamedQuery(name = "Newsfeed.findByUrl", query = "SELECT n FROM Newsfeed n WHERE n.url = :url")})
public class Newsfeed implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "url")
    private String url;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "newsfeedId")
    private List<FrontPageSettings> frontPageSettingsList;

    public Newsfeed() {
    }

    public Newsfeed(Integer id) {
        this.id = id;
    }

    public Newsfeed(Integer id, String url) {
        this.id = id;
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlTransient
    public List<FrontPageSettings> getFrontPageSettingsList() {
        return frontPageSettingsList;
    }

    public void setFrontPageSettingsList(List<FrontPageSettings> frontPageSettingsList) {
        this.frontPageSettingsList = frontPageSettingsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Newsfeed)) {
            return false;
        }
        Newsfeed other = (Newsfeed) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "persistence.entities.Newsfeed[ id=" + id + " ]";
    }
    
}

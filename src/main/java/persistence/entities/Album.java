
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Erika Bourque
 */
@Entity
@Table(name = "album")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Album.findAll", query = "SELECT a FROM Album a")
    , @NamedQuery(name = "Album.findById", query = "SELECT a FROM Album a WHERE a.id = :id")
    , @NamedQuery(name = "Album.findByTitle", query = "SELECT a FROM Album a WHERE a.title = :title")
    , @NamedQuery(name = "Album.findByReleaseDate", query = "SELECT a FROM Album a WHERE a.releaseDate = :releaseDate")
    , @NamedQuery(name = "Album.findByNumTracks", query = "SELECT a FROM Album a WHERE a.numTracks = :numTracks")
    , @NamedQuery(name = "Album.findByDateEntered", query = "SELECT a FROM Album a WHERE a.dateEntered = :dateEntered")
    , @NamedQuery(name = "Album.findByCostPrice", query = "SELECT a FROM Album a WHERE a.costPrice = :costPrice")
    , @NamedQuery(name = "Album.findByListPrice", query = "SELECT a FROM Album a WHERE a.listPrice = :listPrice")
    , @NamedQuery(name = "Album.findBySalePrice", query = "SELECT a FROM Album a WHERE a.salePrice = :salePrice")
    , @NamedQuery(name = "Album.findByRemovalStatus", query = "SELECT a FROM Album a WHERE a.removalStatus = :removalStatus")
    , @NamedQuery(name = "Album.findByRemovalDate", query = "SELECT a FROM Album a WHERE a.removalDate = :removalDate")})
public class Album implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "title")
    private String title;
    @Basic(optional = false)
    @NotNull
    @Column(name = "release_date")
    @Temporal(TemporalType.DATE)
    private Date releaseDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "num_tracks")
    private int numTracks;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_entered")
    @Temporal(TemporalType.DATE)
    private Date dateEntered;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cost_price")
    private double costPrice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "list_price")
    private double listPrice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sale_price")
    private double salePrice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "removal_status")
    private short removalStatus;
    @Column(name = "removal_date")
    @Temporal(TemporalType.DATE)
    private Date removalDate;
    @JoinColumn(name = "artist_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Artist artistId;
    @JoinColumn(name = "genre_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Genre genreId;
    @JoinColumn(name = "recording_label_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RecordingLabel recordingLabelId;
    @JoinColumn(name = "cover_art_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CoverArt coverArtId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "album")
    private List<InvoiceAlbum> invoiceAlbumList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "albumId")
    @OrderBy("albumTrackNumber")
    private List<Track> trackList;

    public Album() {
    }

    public Album(Integer id) {
        this.id = id;
    }

    public Album(Integer id, String title, Date releaseDate, int numTracks, Date dateEntered, double costPrice, double listPrice, double salePrice, short removalStatus) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.numTracks = numTracks;
        this.dateEntered = dateEntered;
        this.costPrice = costPrice;
        this.listPrice = listPrice;
        this.salePrice = salePrice;
        this.removalStatus = removalStatus;
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

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getNumTracks() {
        return numTracks;
    }

    public void setNumTracks(int numTracks) {
        this.numTracks = numTracks;
    }

    public Date getDateEntered() {
        return dateEntered;
    }

    public void setDateEntered(Date dateEntered) {
        this.dateEntered = dateEntered;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getListPrice() {
        return listPrice;
    }

    public void setListPrice(double listPrice) {
        this.listPrice = listPrice;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
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

    public Artist getArtistId() {
        return artistId;
    }

    public void setArtistId(Artist artistId) {
        this.artistId = artistId;
    }

    public Genre getGenreId() {
        return genreId;
    }

    public void setGenreId(Genre genreId) {
        this.genreId = genreId;
    }

    public RecordingLabel getRecordingLabelId() {
        return recordingLabelId;
    }

    public void setRecordingLabelId(RecordingLabel recordingLabelId) {
        this.recordingLabelId = recordingLabelId;
    }

    public CoverArt getCoverArtId() {
        return coverArtId;
    }

    public void setCoverArtId(CoverArt coverArtId) {
        this.coverArtId = coverArtId;
    }

    @XmlTransient
    public List<InvoiceAlbum> getInvoiceAlbumList() {
        return invoiceAlbumList;
    }

    public void setInvoiceAlbumList(List<InvoiceAlbum> invoiceAlbumList) {
        this.invoiceAlbumList = invoiceAlbumList;
    }

    @XmlTransient
    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Album)) {
            return false;
        }
        Album other = (Album) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "persistence.entities.Album[ id=" + id + " ]";
    }
    
}

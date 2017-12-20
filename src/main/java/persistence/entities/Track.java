
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Erika Bourque
 */
@Entity
@Table(name = "track")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Track.findAll", query = "SELECT t FROM Track t")
    , @NamedQuery(name = "Track.findById", query = "SELECT t FROM Track t WHERE t.id = :id")
    , @NamedQuery(name = "Track.findByTitle", query = "SELECT t FROM Track t WHERE t.title = :title")
    , @NamedQuery(name = "Track.findByReleaseDate", query = "SELECT t FROM Track t WHERE t.releaseDate = :releaseDate")
    , @NamedQuery(name = "Track.findByPlayLength", query = "SELECT t FROM Track t WHERE t.playLength = :playLength")
    , @NamedQuery(name = "Track.findByAlbumTrackNumber", query = "SELECT t FROM Track t WHERE t.albumTrackNumber = :albumTrackNumber")
    , @NamedQuery(name = "Track.findByDateEntered", query = "SELECT t FROM Track t WHERE t.dateEntered = :dateEntered")
    , @NamedQuery(name = "Track.findByPartOfAlbum", query = "SELECT t FROM Track t WHERE t.partOfAlbum = :partOfAlbum")
    , @NamedQuery(name = "Track.findByCostPrice", query = "SELECT t FROM Track t WHERE t.costPrice = :costPrice")
    , @NamedQuery(name = "Track.findByListPrice", query = "SELECT t FROM Track t WHERE t.listPrice = :listPrice")
    , @NamedQuery(name = "Track.findBySalePrice", query = "SELECT t FROM Track t WHERE t.salePrice = :salePrice")
    , @NamedQuery(name = "Track.findByRemovalStatus", query = "SELECT t FROM Track t WHERE t.removalStatus = :removalStatus")
    , @NamedQuery(name = "Track.findByRemovalDate", query = "SELECT t FROM Track t WHERE t.removalDate = :removalDate")})
public class Track implements Serializable {

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
    @Size(min = 1, max = 10)
    @Column(name = "play_length")
    private String playLength;
    @Basic(optional = false)
    @NotNull
    @Column(name = "album_track_number")
    private int albumTrackNumber;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_entered")
    @Temporal(TemporalType.DATE)
    private Date dateEntered;
    @Basic(optional = false)
    @NotNull
    @Column(name = "part_of_album")
    private short partOfAlbum;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "trackId")
    private List<Review> reviewList;
    @JoinColumn(name = "album_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Album albumId;
    @JoinColumn(name = "artist_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Artist artistId;
    @JoinColumn(name = "songwriter_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Songwriter songwriterId;
    @JoinColumn(name = "genre_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Genre genreId;
    @JoinColumn(name = "cover_art_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CoverArt coverArtId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "track")
    private List<InvoiceTrack> invoiceTrackList;

    public Track() {
    }

    public Track(Integer id) {
        this.id = id;
    }

    public Track(Integer id, String title, Date releaseDate, String playLength, int albumTrackNumber, Date dateEntered, short partOfAlbum, double costPrice, double listPrice, double salePrice, short removalStatus) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.playLength = playLength;
        this.albumTrackNumber = albumTrackNumber;
        this.dateEntered = dateEntered;
        this.partOfAlbum = partOfAlbum;
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

    public String getPlayLength() {
        return playLength;
    }

    public void setPlayLength(String playLength) {
        this.playLength = playLength;
    }

    public int getAlbumTrackNumber() {
        return albumTrackNumber;
    }

    public void setAlbumTrackNumber(int albumTrackNumber) {
        this.albumTrackNumber = albumTrackNumber;
    }

    public Date getDateEntered() {
        return dateEntered;
    }

    public void setDateEntered(Date dateEntered) {
        this.dateEntered = dateEntered;
    }

    public short getPartOfAlbum() {
        return partOfAlbum;
    }

    public void setPartOfAlbum(short partOfAlbum) {
        this.partOfAlbum = partOfAlbum;
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

    @XmlTransient
    public List<Review> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    public Album getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Album albumId) {
        this.albumId = albumId;
    }

    public Artist getArtistId() {
        return artistId;
    }

    public void setArtistId(Artist artistId) {
        this.artistId = artistId;
    }

    public Songwriter getSongwriterId() {
        return songwriterId;
    }

    public void setSongwriterId(Songwriter songwriterId) {
        this.songwriterId = songwriterId;
    }

    public Genre getGenreId() {
        return genreId;
    }

    public void setGenreId(Genre genreId) {
        this.genreId = genreId;
    }

    public CoverArt getCoverArtId() {
        return coverArtId;
    }

    public void setCoverArtId(CoverArt coverArtId) {
        this.coverArtId = coverArtId;
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
        if (!(object instanceof Track)) {
            return false;
        }
        Track other = (Track) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "persistence.entities.Track[ id=" + id + " ]";
    }
    
}

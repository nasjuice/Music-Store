package com.pandamedia.beans;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import persistence.controllers.TrackJpaController;
import persistence.entities.InvoiceTrack_;
import persistence.entities.Invoice_;
import persistence.entities.Review;
import persistence.entities.Track;
import persistence.entities.Track_;

/**
 * This class will be used as the track backing bean. It can create, update,
 * delete and query tracks.
 *
 * @author Evan Glicakis, Naasir Jusab
 */
@Named("trackBacking")
@SessionScoped
public class TrackBackingBean implements Serializable {

    @Inject
    private TrackJpaController trackController;
    @Inject
    private ClientTrackingBean clientTracking;
    private Track track;
    private List<Track> tracks;
    private List<Track> filteredTracks;
    @PersistenceContext
    private EntityManager em;
    private String genre_string;
    private boolean isTrackSales = true;

    /**
     * This method will initialize a list of tracks that will be used by the
     * data table. PostConstruct is used in methods that need to be executed
     * after dependency injection is done to perform any initialization. In this
     * case, I need the list of tracks after trackController has been injected.
     */
    @PostConstruct
    public void init() {
        this.tracks = trackController.findTrackEntities();
    }

    /**
     * This method will return a track if it exists already. Otherwise, it will
     * return a new track.
     *
     * @return track object
     */
    public Track getTrack() {
        if (track == null) {
            track = new Track();
        }
        return track;
    }

    /**
     * This method will set a list of filtered tracks to change the current list
     * of filtered tracks.
     *
     * @param filteredTracks list of filtered tracks
     */
    public void setFilteredTracks(List<Track> filteredTracks) {
        this.filteredTracks = filteredTracks;
    }

    /**
     * This method will return a list of filtered tracks so that the manager can
     * make searches on tracks.
     *
     * @return list of filteredTracks
     */
    public List<Track> getFilteredTracks() {
        return this.filteredTracks;
    }

    /**
     * This method will return all the tracks in a list so it can be displayed
     * on the data table.
     *
     * @return all tracks in the database
     */
    public List<Track> getTracks() {
        return tracks;
    }

    /**
     * This method will set a list of tracks to make changes to the current list
     * of all tracks.
     *
     * @param tracks all tracks in the database
     */
    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    /**
     * This method will change the current track object.
     *
     * @param track new track object
     */
    public void setTrack(Track track) {
        this.track = track;
    }

    /**
     * sets the track variable and returns the string of the url to the track
     * page
     *
     * @param a
     * @return
     */
    public String trackPage(Track t) {
        track = t;
        clientTracking.peristTracking(t.getGenreId());
        return "track";
    }

    /**
     * used to render the tracks on sale if there are any tracks on sale
     *
     * @return
     */
    public boolean isIsTrackSales() {
        return isTrackSales;
    }

    public void setIsTrackSales(boolean isTrackSales) {
        this.isTrackSales = isTrackSales;
    }

    /**
     * Finds the Track from its id.
     *
     * @param id of the track
     * @return track object
     */
    public Track findTrackById(int id) {
        return trackController.findTrack(id);
    }
    /**
     * Gets the most popular tracks.
     * @author Evan G. Ripped off from Erika's report and modified.
     * @return 
     */
    public List<Track> getPopularTracks() {
        Date startDate = new Date(); //get current date
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Date endDate = cal.getTime();
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Track> trackRoot = query.from(Track.class);
        Join invoiceTrackJoin = trackRoot.join(Track_.invoiceTrackList);
        Join invoiceJoin = invoiceTrackJoin.join(InvoiceTrack_.invoice);
        query.multiselect(cb.sum(invoiceTrackJoin.get(InvoiceTrack_.finalPrice)), trackRoot);
        query.groupBy(trackRoot.get(Track_.id));

        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), endDate, startDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        predicates.add(cb.equal(invoiceTrackJoin.get(InvoiceTrack_.removalStatus), 0));
        predicates.add(cb.equal(trackRoot.get(Track_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        // Order by clause
        query.orderBy(cb.desc(cb.sum(invoiceTrackJoin.get(InvoiceTrack_.finalPrice))));
        List<Track> tracks = new ArrayList<>();
        TypedQuery<Object[]> typedQuery = em.createQuery(query).setMaxResults(6);
        List<Object[]> l = typedQuery.getResultList();
        for (Object[] o : l) {
            tracks.add((Track) o[1]);
        }
        return tracks;
    }

    /**
     * This method will add a track that has been removed. It will change the
     * removal status to 0 which means that it is available for purchase. 1
     * means that it is not available for purchase. It will set the removal date
     * to null since it has not been removed. At the end, the track is set to
     * null so that it does not stay in session scoped and the filtered tracks
     * are regenerated. The return type null should make it stay on the same
     * page.
     *
     * @param id of the track that will be added
     * @return null make it stay on the same page
     */
    public String addItem(Integer id) {
        track = trackController.findTrack(id);
        if (track.getRemovalStatus() != 0) {
            short i = 0;
            track.setRemovalStatus(i);
            track.setRemovalDate(null);

            try {
                trackController.edit(track);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        this.track = null;
        this.filteredTracks = trackController.findTrackEntities();
        return null;
    }

    /**
     * This method will remove a track that has been added. It will change the
     * removal status to 1 which means that it is not available for purchase. 0
     * means that it is available for purchase. It will set the removal date to
     * the date when you clicked on the remove. At the end, the track is set to
     * null so that it does not stay in session scoped and the filtered tracks
     * are regenerated. The return type null should make it stay on the same
     * page.
     *
     * @param id of the track that will be removed
     * @return null make it stay on the same page
     */
    public String removeItem(Integer id) {
        track = trackController.findTrack(id);
        if (track.getRemovalStatus() != 1) {
            short i = 1;
            track.setRemovalStatus(i);
            track.setRemovalDate(Calendar.getInstance().getTime());

            try {
                trackController.edit(track);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        this.track = null;
        this.filteredTracks = trackController.findTrackEntities();
        return null;
    }

    /**
     * This method will set the track so that when the editTrack.xhtml loads.
     * The fields of the page will have values already. All the manager has to
     * do is change the values. The id will make sure that the right track is
     * being edited and the return type will display the edit page for the
     * track.
     *
     * @param id of an track that will be edited
     * @return string that is the edit page for a track
     */
    public String loadEditForIndex(Integer id) {
        this.track = trackController.findTrack(id);
        return "manedittrack";
    }

    /**
     * This method will be called to edit a track. At the end, the track is set
     * to null so that it does not stay in session scoped and the filtered
     * tracks are regenerated.
     *
     * @return string that is the inventory page
     */
    public String edit() {
        try {
            trackController.edit(track);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.track = null;
        this.filteredTracks = trackController.findTrackEntities();
        return "manindex";
    }

    /**
     * This method will be called to create a track. At the end, the track is
     * set to null so that it does not stay in session scoped and the filtered
     * tracks are regenerated.
     *
     * @return string that is the inventory page
     */
    public String create() {
        try {
            trackController.create(track);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.track = null;
        this.filteredTracks = trackController.findTrackEntities();
        return "manindex";
    }

    /**
     * This method is used to return back to the manager home page. Also, the
     * track is set to null so that it does not stay in session scoped and the
     * filtered tracks are regenerated.
     *
     * @return manager home page
     */
    public String back() {
        this.track = null;
        this.filteredTracks = trackController.findTrackEntities();
        return "manindex";
    }

    /**
     * This method is used to return back to the manager sales page.Also, the
     * track is set to null so that it does not stay in session scoped and the
     * filtered tracks are regenerated.
     *
     * @return manager sales page
     */
    public String backSales() {
        this.track = null;
        this.filteredTracks = trackController.findTrackEntities();
        return "mansales";
    }

    /**
     * iterates through the reviews for a track and returns a list of the
     * approved ratings.
     * @author Evan G.
     * @return
     */
    public List<Review> getApprovedReviews() {
        int track_id = track.getId();
        String q = "SELECT r FROM Review r WHERE r.trackId.id = :trackId AND r.approvalStatus = :approval ORDER BY r.dateEntered DESC";
        TypedQuery<Review> query = em.createQuery(q, Review.class);
        query.setParameter("trackId", track_id);
        query.setParameter("approval", 1);
        return query.getResultList();
    }

    /**
     * this method is used to display the stars next to a review on the track
     * page it takes in the review rating, creates a List of elements that JSF
     * will then iterate through with <ui:repeat> this is the safer way of doing
     * it rather than using
     * <c:forEach>
     * @author Evan G.
     * @param rating
     * @return
     */
    public List getStarsList(int rating) {
        List l = new ArrayList();
        for (int i = 1; i <= rating; i++) {
            l.add(i);
        }
        return l;
    }

    /**
     * This method will set the track so that when the editSalesTrack.xhtml
     * loads. The fields of the page will have values already. All the manager
     * has to do is change the values. The id will make sure that the right
     * track is being edited and the return type will display the edit page for
     * the track.
     *
     * @param id of the track that will be edited
     * @return string that represents the page where the sales of a track can be
     * edited
     */
    public String loadEditForSales(Integer id) {
        this.track = trackController.findTrack(id);
        return "maneditsalestrack";
    }

    /**
     * This method will edit the sales of a track, if the sale price is less
     * than the list price. Otherwise, it will just stay on the page until the
     * manager puts a value where the sale price is less than the list price. At
     * the end, the track is set to null so that it does not stay in session
     * scoped and the filtered tracks are regenerated.
     *
     * @return string that is the salesPage.xhtml or null
     */
    public String editSales() {
        double salePrice = track.getSalePrice();
        double listPrice = track.getListPrice();

        //Add a popup msg
        if (salePrice >= listPrice) {
            return null;
        } else {
            try {
                trackController.edit(track);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            this.track = null;
            this.filteredTracks = trackController.findTrackEntities();
            return "mansales";
        }
    }

    /**
     * This method is used to get the total sales of a track to this date. The
     * number formatter is used to make the sales only two digits after the
     * decimal point and if there are no sales made by the track then 0 is
     * returned.
     *
     * @param id of the track whose sales will be displayed
     * @return string that is the sales of the track
     */
    public String getTrackSales(Integer id) {
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Double> query = cb.createQuery(Double.class);
        Root<Track> trackRoot = query.from(Track.class);
        Join invoiceTrackJoin = trackRoot.join(Track_.invoiceTrackList);
        Join invoiceJoin = invoiceTrackJoin.join(InvoiceTrack_.invoice);
        query.select(cb.sum(invoiceTrackJoin.get(InvoiceTrack_.finalPrice)));
        query.groupBy(trackRoot.get(Track_.id));

        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trackRoot.get(Track_.id), id));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        predicates.add(cb.equal(invoiceTrackJoin.get(InvoiceTrack_.removalStatus), 0));
        predicates.add(cb.equal(trackRoot.get(Track_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        TypedQuery<Double> typedQuery = em.createQuery(query);
        NumberFormat formatter = new DecimalFormat("#0.00");

        if (typedQuery.getResultList().size() == 0) {
            return "0.0";
        } else {
            return formatter.format(typedQuery.getResultList().get(0));
        }

    }

    /**
     * This method will return all the tracks in the database so it can be
     * displayed on the data table.
     *
     * @return list of tracks
     */
    public List<Track> getAll() {
        return trackController.findTrackEntities();
    }

    /**
     * Returns a list of tracks that are on sale. checks the database for tracks
     * that have a sale price that is not 0.
     * @author Evan G.
     * @return
     */
    public List<Track> getSaleTracks() {
        String q = "SELECT t FROM Track t WHERE t.salePrice != 0";
        TypedQuery<Track> query = em.createQuery(q, Track.class);
        if (query.getResultList().isEmpty()) {
            isTrackSales = false; // there are no tracks on sale, render false.
        }
        return query.getResultList();
    }

    public String loadCreateTrack() {
        this.track = new Track();
        return "manaddtrack";
    }
}

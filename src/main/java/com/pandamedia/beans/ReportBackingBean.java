package com.pandamedia.beans;

import java.io.Serializable;
import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import persistence.entities.Album;
import persistence.entities.Album_;
import persistence.entities.Artist;
import persistence.entities.Invoice;
import persistence.entities.InvoiceAlbum;
import persistence.entities.InvoiceAlbum_;
import persistence.entities.InvoiceTrack;
import persistence.entities.InvoiceTrackPK_;
import persistence.entities.InvoiceTrack_;
import persistence.entities.Invoice_;
import persistence.entities.ShopUser;
import persistence.entities.ShopUser_;
import persistence.entities.Track;
import persistence.entities.Track_;

/**
 * This class provides common queries for the report pages.
 *
 * @author Erika Bourque
 */
@Named("reports")
@RequestScoped
public class ReportBackingBean implements Serializable {
    private static final Logger LOG = Logger.getLogger("ReportBackingBean.class");

    @PersistenceContext
    private EntityManager em;

    public ReportBackingBean() {
    }

    /**
     * This method returns a list of all the shop users that did not make any
     * purchases in the time frame specified.
     *
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @return The list of shop users
     */
    public List<ShopUser> getZeroUsers(Date startDate, Date endDate) {
        String logMsg = "getZeroUsers\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);

        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ShopUser> query = cb.createQuery(ShopUser.class);
        Root<ShopUser> userRoot = query.from(ShopUser.class);
        query.select(userRoot);

        // Invoice Subquery
        Subquery<Invoice> subquery = query.subquery(Invoice.class);
        Root<Invoice> invoiceRoot = subquery.from(Invoice.class);
        subquery.select(invoiceRoot);

        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceRoot.get(Invoice_.userId), userRoot));
        predicates.add(cb.equal(invoiceRoot.get(Invoice_.removalStatus), 0));        
        // Making Netbeans compiler happy, does not like subquery Root in Criteria Builder's between clause
        Predicate p1 = cb.between(invoiceRoot.get(Invoice_.saleDate).as(Date.class), startDate, endDate);
        predicates.add(p1);
        subquery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        // Putting them together
        query.where(cb.not(cb.exists(subquery)));
        
        TypedQuery<ShopUser> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }

    /**
     * This method returns a list of the all the tracks that have
     * not been purchased in the time frame specified.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate   The report's end date
     * @return          The list of tracks
     */
    public List<Track> getZeroTracks(Date startDate, Date endDate) {
        String logMsg = "getZeroTracks\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);

        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Track> query = cb.createQuery(Track.class);
        Root<Track> trackRoot = query.from(Track.class);
        query.select(trackRoot);

        // Invoice Subquery
        Subquery<InvoiceTrack> subquery = query.subquery(InvoiceTrack.class);
        Root<InvoiceTrack> invoiceTrackRoot = subquery.from(InvoiceTrack.class);
        subquery.select(invoiceTrackRoot);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);

        // Where Clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceTrackRoot.get(InvoiceTrack_.invoiceTrackPK).get(InvoiceTrackPK_.trackId), trackRoot.get(Track_.id)));
        predicates.add(cb.equal(invoiceTrackRoot.get(InvoiceTrack_.removalStatus), 0));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        // Making Netbeans compiler happy, does not like subquery Root in Criteria Builder's between clause
        Predicate p1 = cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate);
        predicates.add(p1);
        subquery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        // Putting them together
        query.where(cb.and(cb.not(cb.exists(subquery)), cb.equal(trackRoot.get(Track_.removalStatus), 0)));

        TypedQuery<Track> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }

    /**
     * This method returns a list of the clients who made purchases in the 
     * time frame specified along with the total amount they spent, in order of
     * most amount spent to least amount spent.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @return          The list of clients with their total amount spent
     */
    public List<Object[]> getTopClients(Date startDate, Date endDate) {
        String logMsg = "getTopClients\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);

        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<ShopUser> userRoot = query.from(ShopUser.class);
        Join invoiceJoin = userRoot.join(ShopUser_.invoiceList);
        query.multiselect(cb.sum(invoiceJoin.get(Invoice_.totalGrossValue)), userRoot);
        query.groupBy(userRoot.get(ShopUser_.id));

        // Where clause
        Predicate p1 = cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate);
        Predicate p2 = cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0);
        query.where(cb.and(p1, p2));

        // Order by clause
        query.orderBy(cb.desc(cb.sum(invoiceJoin.get(Invoice_.totalGrossValue))));

        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }

    /**
     * This method returns a list of the tracks that have been purchased in the 
     * time frame specified along with the total income they generated, in 
     * order of most income to least income.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @return          The list of tracks with their total income
     */
    public List<Object[]> getTopSellersTracks(Date startDate, Date endDate) {
        String logMsg = "getTopSellersTracks\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);

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
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        predicates.add(cb.equal(invoiceTrackJoin.get(InvoiceTrack_.removalStatus), 0));
        predicates.add(cb.equal(trackRoot.get(Track_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        // Order by clause
        query.orderBy(cb.desc(cb.sum(invoiceTrackJoin.get(InvoiceTrack_.finalPrice))));

        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }

    /**
     * This method returns a list of the albums that have been purchased in the 
     * time frame specified along with the total income they generated, in 
     * order of most income to least income.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @return          The list of albums with their total income
     */
    public List<Object[]> getTopSellersAlbums(Date startDate, Date endDate) {
        String logMsg = "getTopSellersAlbums\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);

        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Album> albumRoot = query.from(Album.class);
        Join invoiceAlbumJoin = albumRoot.join(Album_.invoiceAlbumList);
        Join invoiceJoin = invoiceAlbumJoin.join(InvoiceTrack_.invoice);
        query.multiselect(cb.sum(invoiceAlbumJoin.get(InvoiceAlbum_.finalPrice)), albumRoot);
        query.groupBy(albumRoot.get(Album_.id));

        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        predicates.add(cb.equal(invoiceAlbumJoin.get(InvoiceTrack_.removalStatus), 0));
        predicates.add(cb.equal(albumRoot.get(Album_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        // Order by clause
        query.orderBy(cb.desc(cb.sum(invoiceAlbumJoin.get(InvoiceAlbum_.finalPrice))));

        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }

    /**
     * This method returns the total income, cost, and profit of all the
     * track sales within the time frame specified.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @return          The list of totals
     */
    public List<Object[]> getTotalSalesTracks(Date startDate, Date endDate) {
        String logMsg = "getTotalSalesTracks\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceTrack> invoiceTrackRoot = query.from(InvoiceTrack.class);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);

        Expression<Double> finalSum = cb.sum(invoiceTrackRoot.get(InvoiceTrack_.finalPrice));
        Expression<Double> costSum = cb.sum(invoiceTrackRoot.get(InvoiceTrack_.track).get(Track_.costPrice));

        query.multiselect(finalSum,
                costSum,
                cb.diff(finalSum, costSum));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns the total income, cost, and profit of all the
     * album sales within the time frame specified.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @return          The list of totals
     */
    public List<Object[]> getTotalSalesAlbums(Date startDate, Date endDate) {
        String logMsg = "getTotalSalesAlbums\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceAlbum> invoiceAlbumRoot = query.from(InvoiceAlbum.class);
        Join invoiceJoin = invoiceAlbumRoot.join(InvoiceAlbum_.invoice);

        Expression<Double> finalSum = cb.sum(invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice));
        Expression<Double> costSum = cb.sum(invoiceAlbumRoot.get(InvoiceAlbum_.album).get(Album_.costPrice));

        query.multiselect(finalSum,
                costSum,
                cb.diff(finalSum, costSum));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns a list of the invoice details for all tracks that have 
     * been purchased in the time frame specified, in order of the sale date.  
     * This includes the income, cost, and profit of the sale.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @return          The list of invoices
     */
    public List<Object[]> getTotalSalesTracksDetails(Date startDate, Date endDate) {
        String logMsg = "getTotalSalesTracksDetails\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceTrack> invoiceTrackRoot = query.from(InvoiceTrack.class);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);
        Join trackJoin = invoiceTrackRoot.join(InvoiceTrack_.track);
        query.multiselect(invoiceJoin,
                trackJoin,
                invoiceTrackRoot.get(InvoiceTrack_.finalPrice),
                cb.diff(invoiceTrackRoot.get(InvoiceTrack_.finalPrice), trackJoin.get(Track_.costPrice)));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        // Order by clause
        query.orderBy(cb.desc(invoiceJoin.get(Invoice_.saleDate)));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }

    /**
     * This method returns a list of the invoice details for all albums that have 
     * been purchased in the time frame specified, in order of the sale date.  
     * This includes the income, cost, and profit of the sale.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @return          The list of invoices
     */
    public List<Object[]> getTotalSalesAlbumsDetails(Date startDate, Date endDate) {
        String logMsg = "getTotalSalesAlbumsDetails\tStart: " + startDate + "\tEnd: " + endDate;
        LOG.log(Level.INFO, logMsg);

        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceAlbum> invoiceAlbumRoot = query.from(InvoiceAlbum.class);
        Join invoiceJoin = invoiceAlbumRoot.join(InvoiceAlbum_.invoice);
        Join albumJoin = invoiceAlbumRoot.join(InvoiceAlbum_.album);
        query.multiselect(invoiceJoin,
                albumJoin,
                invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice),
                cb.diff(invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice), albumJoin.get(Album_.costPrice)));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        // Order by clause
        query.orderBy(cb.desc(invoiceJoin.get(Invoice_.saleDate)));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns a list of the invoice details for the specified
     * track, sold in the time frame specified, in order of the sale date.  
     * This includes the income, cost, and profit of the sale.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param track     The track desired for the report
     * @return          The list of invoices
     */
    public List<Object[]> getSalesByTrack(Date startDate, Date endDate, Track track)
    {
        if (track == null)
        {
            String logMsg = "getSalesByTrack\tStart: " + startDate + "\tEnd: " + endDate + "\tTrack: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByTrack\tStart: " + startDate + "\tEnd: " + endDate + "\tTrack: " + track.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceTrack> invoiceTrackRoot = query.from(InvoiceTrack.class);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);
        query.multiselect(invoiceJoin,
                invoiceTrackRoot.get(InvoiceTrack_.finalPrice), 
                invoiceTrackRoot.get(InvoiceTrack_.track).get(Track_.costPrice),
                cb.diff(invoiceTrackRoot.get(InvoiceTrack_.finalPrice), invoiceTrackRoot.get(InvoiceTrack_.track).get(Track_.costPrice)));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceTrackRoot.get(InvoiceTrack_.track), track));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        // Order by clause
        query.orderBy(cb.desc(invoiceJoin.get(Invoice_.saleDate)));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns the total income, cost, and profit of all the sales 
     * within the time frame specified for a particular track.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param track     The track desired for the report
     * @return          The list of totals
     */
    public List<Object[]> getSalesByTrackTotals(Date startDate, Date endDate, Track track) {
        if (track == null)
        {
            String logMsg = "getSalesByTrackTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tTrack: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByTrackTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tTrack: " + track.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceTrack> invoiceTrackRoot = query.from(InvoiceTrack.class);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);

        Expression<Double> finalSum = cb.sum(invoiceTrackRoot.get(InvoiceTrack_.finalPrice));
        Expression<Double> costSum = cb.sum(invoiceTrackRoot.get(InvoiceTrack_.track).get(Track_.costPrice));

        query.multiselect(finalSum,
                costSum,
                cb.diff(finalSum, costSum));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceTrackRoot.get(InvoiceTrack_.track), track));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns a list of the invoice details for the specified
     * artist's tracks purchased in the time frame specified, in order of the sale date.  
     * This includes the income, cost, and profit of the sale.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param artist    The artist desired for the report
     * @return          The list of invoices
     */
    public List<Object[]> getSalesByArtistTracks(Date startDate, Date endDate, Artist artist)
    {        
        if (artist == null)
        {
            String logMsg = "getSalesByArtistTracks\tStart: " + startDate + "\tEnd: " + endDate + "\tArtist: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByArtistTracks\tStart: " + startDate + "\tEnd: " + endDate + "\tArtist: " + artist.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceTrack> invoiceTrackRoot = query.from(InvoiceTrack.class);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);
        Join trackJoin = invoiceTrackRoot.join(InvoiceTrack_.track);
        query.multiselect(invoiceJoin,
                trackJoin,
                invoiceTrackRoot.get(InvoiceTrack_.finalPrice), 
                invoiceTrackRoot.get(InvoiceTrack_.track).get(Track_.costPrice),
                cb.diff(invoiceTrackRoot.get(InvoiceTrack_.finalPrice), trackJoin.get(Track_.costPrice)));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trackJoin.get(Track_.artistId), artist));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
                
        // Order by clause
        query.orderBy(cb.desc(invoiceJoin.get(Invoice_.saleDate)));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns the total income, cost, and profit of all the 
     * track sales within the time frame specified for a particular artist.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param artist    The artist desired for the report
     * @return          The list of totals
     */
    public List<Object[]> getSalesByArtistTracksTotals(Date startDate, Date endDate, Artist artist) {
        if (artist == null)
        {
            String logMsg = "getSalesByArtistTracksTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tArtist: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByArtistTracksTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tArtist: " + artist.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceTrack> invoiceTrackRoot = query.from(InvoiceTrack.class);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);
        Join trackJoin = invoiceTrackRoot.join(InvoiceTrack_.track);

        Expression<Double> finalSum = cb.sum(invoiceTrackRoot.get(InvoiceTrack_.finalPrice));
        Expression<Double> costSum = cb.sum(invoiceTrackRoot.get(InvoiceTrack_.track).get(Track_.costPrice));

        query.multiselect(finalSum,
                costSum,
                cb.diff(finalSum, costSum));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trackJoin.get(Track_.artistId), artist));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns a list of the invoice details for the specified
     * artist's albums purchased in the time frame specified, in order of the sale date.  
     * This includes the income, cost, and profit of the sale.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param artist    The artist desired for the report
     * @return          The list of invoices
     */
    public List<Object[]> getSalesByArtistAlbums(Date startDate, Date endDate, Artist artist)
    {
        if (artist == null)
        {
            String logMsg = "getSalesByArtistAlbums\tStart: " + startDate + "\tEnd: " + endDate + "\tArtist: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByArtistAlbums\tStart: " + startDate + "\tEnd: " + endDate + "\tArtist: " + artist.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceAlbum> invoiceAlbumRoot = query.from(InvoiceAlbum.class);
        Join invoiceJoin = invoiceAlbumRoot.join(InvoiceAlbum_.invoice);
        Join albumJoin = invoiceAlbumRoot.join(InvoiceAlbum_.album);
        query.multiselect(invoiceJoin,
                albumJoin,
                invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice), 
                invoiceAlbumRoot.get(InvoiceAlbum_.album).get(Album_.costPrice),
                cb.diff(invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice), albumJoin.get(Album_.costPrice)));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(albumJoin.get(Album_.artistId), artist));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        // Order by clause
        query.orderBy(cb.desc(invoiceJoin.get(Invoice_.saleDate)));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns the total income, cost, and profit of all the 
     * track sales within the time frame specified for a particular artist.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param artist    The artist desired for the report
     * @return          The list of totals
     */
    public List<Object[]> getSalesByArtistAlbumsTotals(Date startDate, Date endDate, Artist artist) {
        if (artist == null)
        {
            String logMsg = "getSalesByArtistAlbumsTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tArtist: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByArtistAlbumsTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tArtist: " + artist.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceAlbum> invoiceAlbumRoot = query.from(InvoiceAlbum.class);
        Join invoiceJoin = invoiceAlbumRoot.join(InvoiceAlbum_.invoice);
        Join albumJoin = invoiceAlbumRoot.join(InvoiceAlbum_.album);

        Expression<Double> finalSum = cb.sum(invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice));
        Expression<Double> costSum = cb.sum(invoiceAlbumRoot.get(InvoiceAlbum_.album).get(Album_.costPrice));

        query.multiselect(finalSum,
                costSum,
                cb.diff(finalSum, costSum));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(albumJoin.get(Track_.artistId), artist));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns a list of the invoice details for the specified
     * album, sold in the time frame specified, in order of the sale date.  
     * This includes the income, cost, and profit of the sale.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param album     The album desired for the report
     * @return          The list of invoices
     */    
    public List<Object[]> getSalesByAlbum(Date startDate, Date endDate, Album album)
    {
        if (album == null)
        {
            String logMsg = "getSalesByAlbum\tStart: " + startDate + "\tEnd: " + endDate + "\tAlbum: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByAlbum\tStart: " + startDate + "\tEnd: " + endDate + "\tAlbum: " + album.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceAlbum> invoiceAlbumRoot = query.from(InvoiceAlbum.class);
        Join invoiceJoin = invoiceAlbumRoot.join(InvoiceAlbum_.invoice);
        query.multiselect(invoiceJoin,
                invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice), 
                invoiceAlbumRoot.get(InvoiceAlbum_.album).get(Album_.costPrice),
                cb.diff(invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice), invoiceAlbumRoot.get(InvoiceAlbum_.album).get(Album_.costPrice)));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceAlbumRoot.get(InvoiceAlbum_.album), album));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        // Order by clause
        query.orderBy(cb.desc(invoiceJoin.get(Invoice_.saleDate)));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns the total income, cost, and profit of all the sales 
     * within the time frame specified for a particular album.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param album     The album desired for the report
     * @return          The list of totals
     */
    public List<Object[]> getSalesByAlbumTotals(Date startDate, Date endDate, Album album) {
        if (album == null)
        {
            String logMsg = "getSalesByAlbumTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tAlbum: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByAlbumTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tAlbum: " + album.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceAlbum> invoiceAlbumRoot = query.from(InvoiceAlbum.class);
        Join invoiceJoin = invoiceAlbumRoot.join(InvoiceAlbum_.invoice);

        Expression<Double> finalSum = cb.sum(invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice));
        Expression<Double> costSum = cb.sum(invoiceAlbumRoot.get(InvoiceAlbum_.album).get(Album_.costPrice));

        query.multiselect(finalSum,
                costSum,
                cb.diff(finalSum, costSum));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceAlbumRoot.get(InvoiceAlbum_.album), album));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns a list of the invoice details for the specified
     * client's tracks purchased in the time frame specified, in order of the sale date.  
     * This includes the income, cost, and profit of the sale.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param user    The client desired for the report
     * @return          The list of invoices
     */
    public List<Object[]> getSalesByClientTracks(Date startDate, Date endDate, ShopUser user)
    {        
        if (user == null)
        {
            String logMsg = "getSalesByClientTracks\tStart: " + startDate + "\tEnd: " + endDate + "\tClient: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByClientTracks\tStart: " + startDate + "\tEnd: " + endDate + "\tClient: " + user.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceTrack> invoiceTrackRoot = query.from(InvoiceTrack.class);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);
        Join trackJoin = invoiceTrackRoot.join(InvoiceTrack_.track);
        query.multiselect(invoiceJoin,
                trackJoin,
                invoiceTrackRoot.get(InvoiceTrack_.finalPrice), 
                invoiceTrackRoot.get(InvoiceTrack_.track).get(Track_.costPrice),
                cb.diff(invoiceTrackRoot.get(InvoiceTrack_.finalPrice), trackJoin.get(Track_.costPrice)));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.userId), user));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
                
        // Order by clause
        query.orderBy(cb.desc(invoiceJoin.get(Invoice_.saleDate)));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns the total income, cost, and profit of all the 
     * track sales within the time frame specified for a particular client.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param user    The client desired for the report
     * @return          The list of totals
     */
    public List<Object[]> getSalesByClientTracksTotals(Date startDate, Date endDate, ShopUser user) {
        if (user == null)
        {
            String logMsg = "getSalesByClientTracksTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tClient: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByClientTracksTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tClient: " + user.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceTrack> invoiceTrackRoot = query.from(InvoiceTrack.class);
        Join invoiceJoin = invoiceTrackRoot.join(InvoiceTrack_.invoice);

        Expression<Double> finalSum = cb.sum(invoiceTrackRoot.get(InvoiceTrack_.finalPrice));
        Expression<Double> costSum = cb.sum(invoiceTrackRoot.get(InvoiceTrack_.track).get(Track_.costPrice));

        query.multiselect(finalSum,
                costSum,
                cb.diff(finalSum, costSum));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.userId), user));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns a list of the invoice details for the specified
     * artist's albums purchased in the time frame specified, in order of the sale date.  
     * This includes the income, cost, and profit of the sale.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param user    The user desired for the report
     * @return          The list of invoices
     */
    public List<Object[]> getSalesByClientAlbums(Date startDate, Date endDate, ShopUser user)
    {
        if (user == null)
        {
            String logMsg = "getSalesByClientAlbums\tStart: " + startDate + "\tEnd: " + endDate + "\tClient: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByClientAlbums\tStart: " + startDate + "\tEnd: " + endDate + "\tClient: " + user.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceAlbum> invoiceAlbumRoot = query.from(InvoiceAlbum.class);
        Join invoiceJoin = invoiceAlbumRoot.join(InvoiceAlbum_.invoice);
        Join albumJoin = invoiceAlbumRoot.join(InvoiceAlbum_.album);
        query.multiselect(invoiceJoin,
                albumJoin,
                invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice), 
                invoiceAlbumRoot.get(InvoiceAlbum_.album).get(Album_.costPrice),
                cb.diff(invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice), albumJoin.get(Album_.costPrice)));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.userId), user));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        // Order by clause
        query.orderBy(cb.desc(invoiceJoin.get(Invoice_.saleDate)));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
    
    /**
     * This method returns the total income, cost, and profit of all the 
     * track sales within the time frame specified for a particular artist.
     * 
     * @author Erika Bourque
     * @param startDate The report's start date
     * @param endDate The report's end date
     * @param user    The artist desired for the report
     * @return          The list of totals
     */
    public List<Object[]> getSalesByClientAlbumsTotals(Date startDate, Date endDate, ShopUser user) {
        if (user == null)
        {
            String logMsg = "getSalesByClientAlbumsTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tClient: null";
            LOG.log(Level.INFO, logMsg);
            return null;
        }
        
        String logMsg = "getSalesByClientAlbumsTotals\tStart: " + startDate + "\tEnd: " + endDate + "\tClient: " + user.getId();
        LOG.log(Level.INFO, logMsg);
        
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvoiceAlbum> invoiceAlbumRoot = query.from(InvoiceAlbum.class);
        Join invoiceJoin = invoiceAlbumRoot.join(InvoiceAlbum_.invoice);

        Expression<Double> finalSum = cb.sum(invoiceAlbumRoot.get(InvoiceAlbum_.finalPrice));
        Expression<Double> costSum = cb.sum(invoiceAlbumRoot.get(InvoiceAlbum_.album).get(Album_.costPrice));

        query.multiselect(finalSum,
                costSum,
                cb.diff(finalSum, costSum));
        
        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.userId), user));
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), startDate, endDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }
}
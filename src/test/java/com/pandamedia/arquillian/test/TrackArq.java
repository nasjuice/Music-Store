
package com.pandamedia.arquillian.test;

import com.pandamedia.beans.InvoiceBackingBean;
import com.pandamedia.beans.ReportBackingBean;
import com.pandamedia.beans.TrackBackingBean;
import com.pandamedia.beans.purchasing.ShoppingCart;
import com.pandamedia.commands.ChangeLanguage;
import com.pandamedia.converters.AlbumConverter;
import com.pandamedia.filters.LoginFilter;
import com.pandamedia.utilities.Messages;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import persistence.controllers.AlbumJpaController;
import persistence.controllers.ArtistJpaController;
import persistence.controllers.CoverArtJpaController;
import persistence.controllers.GenreJpaController;
import persistence.controllers.InvoiceJpaController;
import persistence.controllers.InvoiceTrackJpaController;
import persistence.controllers.ReviewJpaController;
import persistence.controllers.ShopUserJpaController;
import persistence.controllers.SongwriterJpaController;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Album;
import persistence.entities.Invoice;
import persistence.entities.InvoiceAlbum;
import persistence.entities.InvoiceTrack;
import persistence.entities.Review;
import persistence.entities.Track;

/**
 *
 * @author Naasir Jusab
 */
@RunWith(Arquillian.class)
public class TrackArq {
    
            
    @Resource(name = "java:app/jdbc/pandamedialocal")
    private DataSource ds;
    @Inject
    private TrackBackingBean trackBacking;
    @Inject
    private AlbumJpaController albumController;
    @Inject
    private ArtistJpaController artistController;
    @Inject
    private SongwriterJpaController songwriterController;
    @Inject
    private GenreJpaController genreController;
    @Inject
    private CoverArtJpaController coverArtController;
   @Inject
   private ShopUserJpaController userController;
   @Inject
   private ReviewJpaController reviewController;
   @Inject
   private InvoiceJpaController invoiceController;
   @Inject
   private InvoiceTrackJpaController invoiceTrackController;
   @Inject
   private InvoiceBackingBean invoiceBacking;
    
    @Deployment
    public static WebArchive deploy() {
        // Use an alternative to the JUnit assert library called AssertJ
        // Need to reference MySQL driver and jodd as it is not part of GlassFish
        final File[] dependencies = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .resolve(new String[]{
                        "org.assertj:assertj-core", "org.jodd:jodd-mail"}).withoutTransitivity()
                .asFile();

        // For testing Arquillian prefers a resources.xml file over a
        // context.xml
        // Actual file name is resources-mysql-ds.xml in the test/resources
        // folder
        // The SQL script to create the database is also in this folder
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .setWebXML(new File("src/main/webapp/WEB-INF/web.xml"))
                .addPackage(ReportBackingBean.class.getPackage())
                .addPackage(ChangeLanguage.class.getPackage())
                .addPackage(AlbumConverter.class.getPackage())
                .addPackage(LoginFilter.class.getPackage())
                .addPackage(Messages.class.getPackage())
                .addPackage(ShopUserJpaController.class.getPackage())
                .addPackage(RollbackFailureException.class.getPackage())
                .addPackage(Track.class.getPackage())
                .addPackage(ShoppingCart.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/glassfish-resources.xml"), "glassfish-resources.xml")
                .addAsResource(new File("src/test/resources-glassfish-remote/test-persistence.xml"), "META-INF/persistence.xml")
//                .addAsResource(new File("src/main/resources/META-INF/persistence.xml"), "META-INF/persistence.xml")
                .addAsResource("createtestdatabase.sql")
                .addAsLibraries(dependencies);        
        return webArchive;
    }
    
    /**
     * This routine is courtesy of Bartosz Majsak who also solved my Arquillian
     * remote server problem
     */
    @Before
    public void seedDatabase() {
        final String seedCreateScript = loadAsString("createtestdatabase.sql");
        //final String seedDataScript = loadAsString("inserttestingdata.sql");

        try (Connection connection = ds.getConnection()) {
            for (String statement : splitStatements(new StringReader(
                    seedCreateScript), ";")) {
                connection.prepareStatement(statement).execute();
                System.out.println("Statement successful: " + statement);
            }
            
//            for (String statement : splitStatements(new StringReader(
//                    seedDataScript), ";")) {
//                connection.prepareStatement(statement).execute();
//            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed seeding database", e);
        }
        //System.out.println("Seeding works");
    }

    /**
     * The following methods support the seedDatabse method
     */
    private String loadAsString(final String path) {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(path)) {
            return new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close input stream.", e);
        }
    }

    private List<String> splitStatements(Reader reader,
            String statementDelimiter) {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        final StringBuilder sqlStatement = new StringBuilder();
        final List<String> statements = new LinkedList<>();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isComment(line)) {
                    continue;
                }
                sqlStatement.append(line);
                if (line.endsWith(statementDelimiter)) {
                    statements.add(sqlStatement.toString());
                    sqlStatement.setLength(0);
                }
            }
//            System.out.println(statements);
            return statements;
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing sql", e);
        }
    }

    private boolean isComment(final String line) {
        return line.startsWith("--") || line.startsWith("//")
                || line.startsWith("/*");
    }

    
    @Test
    public void testAddItem()
    {
        trackBacking.addItem(1);
        Track track = trackBacking.findTrackById(1);

        assertThat(track.getRemovalStatus()).isEqualTo((short)0);

    }
    
    @Test
    public void testRemoveItem()
    {
        trackBacking.removeItem(1);
        Track track = trackBacking.findTrackById(1);
        assertEquals(track.getRemovalStatus(),1);
    }
    
    @Test
    public void testEdit()
    {
        Date date = Calendar.getInstance().getTime();
        short status = 1;
        Track t = trackBacking.findTrackById(1);
        
        t.setTitle("haha");
        t.setReleaseDate(date);
        t.setPlayLength("1:46");
        t.setDateEntered(date);
        t.setPartOfAlbum(status);
        t.setAlbumTrackNumber(1);
        t.setCostPrice(1.0);
        t.setListPrice(1.5);
        t.setSalePrice(0);
        t.setRemovalStatus(status);
        t.setRemovalDate(date);
        t.setAlbumId(albumController.findAlbum(1));
        t.setArtistId(artistController.findArtist(1));
        t.setSongwriterId(songwriterController.findSongwriter(1));
        t.setGenreId(genreController.findGenre(1));
        t.setCoverArtId(coverArtController.findCoverArt(1));
        
        trackBacking.setTrack(t);
        trackBacking.edit();
        Track editedTrack = trackBacking.findTrackById(1);
        
        assertThat(t).isEqualTo(editedTrack);
    }
    
    @Test
    public void testCreate()
    {
        Date date = Calendar.getInstance().getTime();
        short status = 1;
        Track t = new Track();
        
        t.setTitle("hehe");
        t.setReleaseDate(date);
        t.setPlayLength("1:45");
        t.setDateEntered(date);
        t.setPartOfAlbum(status);
        t.setAlbumTrackNumber(1);
        t.setCostPrice(1.0);
        t.setListPrice(1.5);
        t.setSalePrice(0);
        t.setRemovalStatus(status);
        t.setRemovalDate(date);
        t.setAlbumId(albumController.findAlbum(1));
        t.setArtistId(artistController.findArtist(1));
        t.setSongwriterId(songwriterController.findSongwriter(1));
        t.setGenreId(genreController.findGenre(1));
        t.setCoverArtId(coverArtController.findCoverArt(1));      
        
        trackBacking.setTrack(t);
        trackBacking.create();
        List<Track> list = trackBacking.getAll();
        
        assertThat(list.get(list.size()-1)).isEqualTo(t);
    }
    
    @Test
    public void testApprovedReviews()
    {
        Date date = Calendar.getInstance().getTime();
        short status = 1;
        Review review = new Review();
        review.setDateEntered(date);
        review.setRating(1);
        review.setReviewContent("ahhahaha hohoho");
        review.setApprovalStatus(status);
        review.setTrackId(trackBacking.findTrackById(1));
        review.setUserId(userController.findShopUser(1));
        
        try
        {
            reviewController.create(review);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        trackBacking.setTrack(trackBacking.findTrackById(1));
        List<Review> approvedReviews = trackBacking.getApprovedReviews();

        boolean isValid = false;
        for(Review approvedReview:approvedReviews)
        {
            if(approvedReview.equals(review))
                isValid = true;
            
        }
        assertTrue(isValid);
        
    }
    
    @Test
    public void testEditSales()
    {      
        Track t = trackBacking.findTrackById(1);
        t.setSalePrice(0);
        
        trackBacking.setTrack(t);
        trackBacking.edit();
        Track editedTrack = trackBacking.findTrackById(1);
        
        assertEquals(t,editedTrack);
    }
    
    @Test
    public void testTrackSales()
    {
        Date date = Calendar.getInstance().getTime();
        short status = 1;
        short removalStatus = 0;
        Track t = new Track();
        
        t.setTitle("hehe");
        t.setReleaseDate(date);
        t.setPlayLength("1:45");
        t.setDateEntered(date);
        t.setPartOfAlbum(status);
        t.setAlbumTrackNumber(1);
        t.setCostPrice(1.0);
        t.setListPrice(1.5);
        t.setSalePrice(0);
        t.setRemovalStatus(removalStatus);
        t.setRemovalDate(null);
        t.setAlbumId(albumController.findAlbum(1));
        t.setArtistId(artistController.findArtist(1));
        t.setSongwriterId(songwriterController.findSongwriter(1));
        t.setGenreId(genreController.findGenre(1));
        t.setCoverArtId(coverArtController.findCoverArt(1));  
        
        trackBacking.setTrack(t);
        trackBacking.create();
        
        List<Track> list = trackBacking.getAll();
        
        InvoiceTrack invT = new InvoiceTrack();
        invT.setTrack(list.get(list.size()-1));
        invT.setRemovalStatus(removalStatus);
        invT.setRemovalDate(null);
        invT.setInvoice(invoiceController.findInvoice(1));
        invT.setFinalPrice(23.00);
        
        try
        {
            invoiceTrackController.create(invT);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        assertEquals(trackBacking.getTrackSales(list.get(list.size()-1).getId()), "23.00");    
    
    }
    
    @Test
    public void testSaleTrack()
    {
        Track t1= trackBacking.findTrackById(1);
        t1.setSalePrice(0.55);
        trackBacking.setTrack(t1);
        trackBacking.edit();
        
        Track t2= trackBacking.findTrackById(2);
        t2.setSalePrice(0.69);
        trackBacking.setTrack(t2);
        trackBacking.edit();
        
        List<Track> saleTracks = trackBacking.getSaleTracks();
        
        assertEquals(saleTracks.size(),2);
    }
    
    @Test
    /**
     *@author Evan G.
     */
    public void testGetPopularTracks(){
        /*
        Alright so for popular tracks, we get the most popular tracks of that week,
        our local database does not have any invoices from this week, or anyweek of testing
        so we need to create an invoice within this week (current date = March 31) and set the
        invoice track final price to be something absurdly high, the invoice_track should  have two albums
        that will be checked to test
         */
        short i = 0;
        Invoice inv = new Invoice();
        // only the date is important here as the invoice table is just used to
        // find by date
        inv.setSaleDate(Calendar.getInstance().getTime());
        inv.setTotalNetValue(24);
        inv.setPstTax(10);
        inv.setGstTax(10);
        inv.setHstTax(10);
        inv.setTotalGrossValue(35);
        inv.setRemovalStatus(i);
        inv.setRemovalDate(null);
        inv.setUserId(userController.findShopUser(1));

        try {
            invoiceController.create(inv);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        List<Invoice> list = invoiceBacking.getAll();
        // get the last invoice
        inv = list.get(list.size() - 1);

        short rmsai = 0;
        // create an invoice of an album
        InvoiceTrack it = new InvoiceTrack();
        it.setTrack(trackBacking.findTrackById(17));//Track: Title: Boomin
        it.setInvoice(inv);
        it.setRemovalDate(null);
        it.setRemovalStatus(rmsai);
        it.setFinalPrice(900.99); // should be highest

        InvoiceTrack it2 = new InvoiceTrack();
        it2.setTrack(trackBacking.findTrackById(63));//Track: Title: Rather be
        it2.setInvoice(inv);
        it2.setRemovalDate(null);
        it2.setRemovalStatus(rmsai);
        it2.setFinalPrice(850.89); // second highest

        try {
            invoiceTrackController.create(it2);
            invoiceTrackController.create(it);
        } catch (Exception ex) {
            Logger.getLogger(AlbumArq.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Track> expected = new ArrayList(); // expected highest selling tracks to be 14 and 6 since they have really high invoice album sales prices
        expected.add(trackBacking.findTrackById(17));
        expected.add(trackBacking.findTrackById(63));    
        List<Track> actual = trackBacking.getPopularTracks().subList(0, 2);
        
        assertEquals(expected, actual);
    }
    
    

}

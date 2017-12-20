package com.pandamedia.arquillian.test;

import com.pandamedia.beans.AlbumBackingBean;
import com.pandamedia.beans.InvoiceBackingBean;
import com.pandamedia.beans.ReportBackingBean;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import persistence.controllers.AlbumJpaController;
import persistence.controllers.ArtistJpaController;
import persistence.controllers.CoverArtJpaController;
import persistence.controllers.GenreJpaController;
import persistence.controllers.InvoiceAlbumJpaController;
import persistence.controllers.InvoiceJpaController;
import persistence.controllers.RecordingLabelJpaController;
import persistence.controllers.ShopUserJpaController;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Album;
import persistence.entities.Artist;
import persistence.entities.Invoice;
import persistence.entities.InvoiceAlbum;
import persistence.entities.Track;

/**
 *
 * @author Evan Glicakis
 */
@RunWith(Arquillian.class)
public class AlbumArq {

    @Resource(name = "java:app/jdbc/pandamedialocal")
    private DataSource ds;
    @Inject
    private AlbumBackingBean albumBacking;
    @Inject
    private AlbumJpaController albumController;
    @Inject
    private ArtistJpaController artistController;
    @Inject
    private GenreJpaController genreController;
    @Inject
    private RecordingLabelJpaController recordingLabelController;
    @Inject
    private CoverArtJpaController coverartController;
    @Inject
    private ShopUserJpaController userController;
    @Inject
    private InvoiceBackingBean invoiceBacking;
    @Inject
    private InvoiceJpaController invoiceController;
    @Inject
    private InvoiceAlbumJpaController invoiceAlbumController;

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

    /**
     * Tests the method used to re-add item that has been removed.
     */
    @Test
    public void testAddItem() {
        albumBacking.addItem(1);
        Album a = albumController.findAlbum(1);
        assertThat(a.getRemovalStatus()).isEqualTo((short)0);
    }

    /**
     * Tests the method used to remove an album.
     */
    @Test
    public void testRemoveItem() {
        albumBacking.removeItem(1);
        Album a = albumController.findAlbum(1);
                assertThat(a.getRemovalStatus()).isEqualTo((short)1);
    }

    @Test
    public void testEditAlbum() {
        short removalStatus = 1;
        Album a = albumController.findAlbum(1);
        Date releasedate = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 2);
        Date dateEntered = cal.getTime();

        a.setTitle("Editied album");
        a.setReleaseDate(releasedate);
        a.setArtistId(artistController.findArtist(1));
        a.setGenreId(genreController.findGenre(1));
        a.setRecordingLabelId(recordingLabelController.findRecordingLabel(1));
        a.setNumTracks(1);
        a.setCoverArtId(coverartController.findCoverArt(1));
        a.setDateEntered(dateEntered);
        a.setCostPrice(1.00);
        a.setSalePrice(0.5);
        a.setListPrice(0.5);
        a.setRemovalStatus(removalStatus);
        a.setRemovalDate(dateEntered);
        // get the track we just edited to compare if the edits worked.
        albumBacking.setAlbum(a);
        albumBacking.edit();
        Album editedAlbum = albumController.findAlbum(1);

          assertThat(a).isEqualTo(editedAlbum);
    }

    @Test
    public void testGetAlbumsOnSale() {
        // currently on localhost database there is no albums on sale.
        //adding two (2) albums with sales to database
        Album a1 = albumController.findAlbum(1);
        a1.setSalePrice(0.10);
        albumBacking.setAlbum(a1);
        albumBacking.edit();

        Album a2 = albumController.findAlbum(2);
        a2.setSalePrice(0.15);
        albumBacking.setAlbum(a2);
        albumBacking.edit();
        // get albums sales should be of size 2
        List<Album> sales = albumBacking.getSaleAlbums();
        assertThat(2).isEqualTo(sales.size());
    }

    @Test
    public void testGetAlbumsFromGenre() {
        //set the genre of albums to grab to "Punk"
        //there are 4 albums of the punk rock genre in the database.
        albumBacking.setGenreString("Punk");
        List<Album> genreAlbums = albumBacking.getAlbumFromGenre();
        assertThat(4).isEqualTo(genreAlbums.size());
    }

    @Test
    public void testGetLatestAlbums() {
        //I'm going to edit an album with the current date and check if that is
        // the latest album, it should be.
        Album a = albumController.findAlbum(5);
        Date currDate = new Date();
        a.setReleaseDate(currDate);
        albumBacking.setAlbum(a);
        albumBacking.edit();
        List latest = albumBacking.getLatestAlbums();
        assertThat(latest.get(0)).isEqualTo(a);
    }

    @Test
    public void testGetAlbumsFromArtist() {
        // select the number of albums from artist 88 finger louie
        Artist artist = artistController.findArtist(1);
        List artsitAlbums = albumBacking.albumsFromArtist(artist);
        //should be one (1) album.
        assertThat(artsitAlbums.size()).isEqualTo(1);
    }

    @Test
    public void testGetAlbumSales() {
        // total sales of album with id = 1 (.5 the gray chaper) is $668.30
        String amount = albumBacking.getAlbumSales(1);
        assertThat(amount).isEqualTo("668.30");
    }

    @Test
    public void testPopularAlbums() {
        /*
        Alright so for popular albums, we get the most popular album of that week,
        our local database does not have any invoices from this week, or anyweek of testing
        so we need to create an invoice within this week (current date = March 31) and set the
        invoice album final price to be something absurdly high, the invoice_album should  have two albums
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
        InvoiceAlbum ia = new InvoiceAlbum();
        ia.setAlbum(albumController.findAlbum(14));//Album: Title: Mobilize
        ia.setInvoice(inv);
        ia.setRemovalDate(null);
        ia.setRemovalStatus(rmsai);
        ia.setFinalPrice(900.99); // should be highest

        InvoiceAlbum ia2 = new InvoiceAlbum();
        ia2.setAlbum(albumController.findAlbum(6));//Album: Title: Back on the Streets
        ia2.setInvoice(inv);
        ia2.setRemovalDate(null);
        ia2.setRemovalStatus(rmsai);
        ia2.setFinalPrice(850.89); // second highest

        try {
            invoiceAlbumController.create(ia2);
            invoiceAlbumController.create(ia);
        } catch (Exception ex) {
            Logger.getLogger(AlbumArq.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Album> expected = new ArrayList(); // expected highest seeling albums to be 14 and 6 since they have really high invoice album sales prices
        expected.add(albumController.findAlbum(14));
        expected.add(albumController.findAlbum(6));
        
        List<Album> actual = albumBacking.getPopularAlbums().subList(0, 2);
        
        assertThat(expected).isEqualTo(actual);

    }
}

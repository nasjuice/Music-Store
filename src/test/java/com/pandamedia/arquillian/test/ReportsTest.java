package com.pandamedia.arquillian.test;

import com.pandamedia.beans.ReportBackingBean;
import com.pandamedia.beans.purchasing.ShoppingCart;
import com.pandamedia.commands.ChangeLanguage;
import com.pandamedia.converters.AlbumConverter;
import com.pandamedia.filters.LoginFilter;
import com.pandamedia.utilities.Messages;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.data.Offset;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
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
import persistence.controllers.InvoiceTrackJpaController;
import persistence.controllers.ProvinceJpaController;
import persistence.controllers.RecordingLabelJpaController;
import persistence.controllers.ShopUserJpaController;
import persistence.controllers.SongwriterJpaController;
import persistence.controllers.TrackJpaController;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Album;
import persistence.entities.Invoice;
import persistence.entities.InvoiceAlbum;
import persistence.entities.InvoiceAlbumPK;
import persistence.entities.InvoiceTrack;
import persistence.entities.InvoiceTrackPK;
import persistence.entities.ShopUser;
import persistence.entities.Track;

/**
 * This class tests all the methods in the ReportBackingBean class.
 *
 * @author Erika Bourque
 */
@RunWith(Arquillian.class)
@Ignore
public class ReportsTest {

    // TO TEST ON WALDO comment and uncomment the @Resources
    // AND the persistence XMLs, both needed to work
//    @Resource(name = "java:app/jdbc/waldo2g4w17")
    @Resource(name = "java:app/jdbc/pandamedialocal")
    private DataSource ds;
    @Inject
    private ReportBackingBean reports;
    @Inject
    private ShopUserJpaController userJpa;
    @Inject
    private ProvinceJpaController provinceJpa;
    @Inject
    private InvoiceJpaController invoiceJpa;
    @Inject
    private InvoiceTrackJpaController invoiceTrackJpa;
    @Inject
    private AlbumJpaController albumJpa;
    @Inject
    private ArtistJpaController artistJpa;
    @Inject
    private CoverArtJpaController coverJpa;
    @Inject
    private GenreJpaController genreJpa;
    @Inject
    private SongwriterJpaController songwriterJpa;
    @Inject
    private TrackJpaController trackJpa;
    @Inject
    private RecordingLabelJpaController recordingJpa;
    @Inject
    private InvoiceAlbumJpaController invoiceAlbumJpa;

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

        try (Connection connection = ds.getConnection()) {
            for (String statement : splitStatements(new StringReader(
                    seedCreateScript), ";")) {
                connection.prepareStatement(statement).execute();
                System.out.println("Statement successful: " + statement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed seeding database", e);
        }
    }

    /**
     * The following methods support the seedDatabase method
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
    public void getZeroUsersContainsTest() throws Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        createNewInvoice(saleDate, 10, 11, test);

        // Action
        List<ShopUser> list = reports.getZeroUsers(start, end);

        // Assert
        assertThat(list.contains(test)).isTrue();
    }

    @Test
    public void getZeroUsersNotContainsTest() throws Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        createNewInvoice(saleDate, 10, 11, test);

        // Action
        List<ShopUser> list = reports.getZeroUsers(start, end);

        // Assert
        assertThat(list.contains(test)).isFalse();
    }

    @Test
    public void getZeroTracksContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = createTestTrack();
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Track> list = reports.getZeroTracks(start, end);

        // Assert
        assertThat(list.contains(track)).isTrue();
    }

    @Test
    public void getZeroTracksNotContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = createTestTrack();
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Track> list = reports.getZeroTracks(start, end);

        // Assert
        assertThat(list.contains(track)).isFalse();
    }

    @Test
    public void getTopClientsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        createNewInvoice(saleDate, 10, 11, test);

        // Action
        List<Object[]> list = reports.getTopClients(start, end);
        // obj[0] is amount sold, obj[1] is user
        for (Object[] obj : list) {
            if (obj[1].equals(test)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getTopClientsNotContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        createNewInvoice(saleDate, 10, 11, test);

        // Action
        List<Object[]> list = reports.getTopClients(start, end);
        // obj[0] is amount sold, obj[1] is user
        for (Object[] obj : list) {
            if (obj[1].equals(test)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getTopSellersTracksContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = createTestTrack();
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getTopSellersTracks(start, end);
        // obj[0] is amount sold, obj[1] is track
        for (Object[] obj : list) {
            if (obj[1].equals(track)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getTopSellersTracksNotContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = createTestTrack();
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getTopSellersTracks(start, end);
        // obj[0] is amount sold, obj[1] is track
        for (Object[] obj : list) {
            if (obj[1].equals(track)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getTopSellersAlbumsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = createTestAlbum();
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getTopSellersAlbums(start, end);
        // obj[0] is amount sold, obj[1] is album
        for (Object[] obj : list) {
            if (obj[1].equals(album)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getTopSellersAlbumsNotContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = createTestAlbum();
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getTopSellersAlbums(start, end);
        // obj[0] is amount sold, obj[1] is album
        for (Object[] obj : list) {
            if (obj[1].equals(album)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getTotalSalesAlbumsContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        double previousFinal = 1336.9100000000005;
        double albumFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = createTestAlbum();
        createNewInvoiceAlbum(invoice, album, albumFinal);

        // Action
        List<Object[]> list = reports.getTotalSalesAlbums(start, end);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal + albumFinal, Offset.offset(0.001));
    }

    @Test
    public void getTotalSalesAlbumsNotContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        double previousFinal = 1336.9100000000005;
        double albumFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = createTestAlbum();
        createNewInvoiceAlbum(invoice, album, albumFinal);

        // Action
        List<Object[]> list = reports.getTotalSalesAlbums(start, end);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal, Offset.offset(0.001));
    }

    @Test
    public void getTotalSalesTracksContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        double previousFinal = 513.1700000000001;
        double trackFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = createTestTrack();
        createNewInvoiceTrack(invoice, track, trackFinal);

        // Action
        List<Object[]> list = reports.getTotalSalesTracks(start, end);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal + trackFinal, Offset.offset(0.001));
    }

    @Test
    public void getTotalSalesTracksNotContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        double previousFinal = 513.1700000000001;
        double trackFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = createTestTrack();
        createNewInvoiceTrack(invoice, track, trackFinal);

        // Action
        List<Object[]> list = reports.getTotalSalesTracks(start, end);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal, Offset.offset(0.001));
    }

    @Test
    public void getTotalSalesTracksDetailsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = createTestTrack();
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getTotalSalesTracksDetails(start, end);
        // obj[1] is track, rest is other details
        for (Object[] obj : list) {
            if (obj[1].equals(track)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getTotalSalesTracksDetailsNotContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = createTestTrack();
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getTotalSalesTracksDetails(start, end);
        // obj[1] is track, rest is other details
        for (Object[] obj : list) {
            if (obj[1].equals(track)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getTotalSalesAlbumsDetailsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = createTestAlbum();
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getTotalSalesAlbumsDetails(start, end);
        // obj[0] is amount sold, obj[1] is album
        for (Object[] obj : list) {
            if (obj[1].equals(album)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getTotalSalesAlbumsDetailsNotContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = createTestAlbum();
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getTotalSalesAlbumsDetails(start, end);
        // obj[0] is amount sold, obj[1] is album
        for (Object[] obj : list) {
            if (obj[1].equals(album)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getSalesByAlbumDetailsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByAlbum(start, end, album);
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getSalesByAlbumDetailsContainsNotTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByAlbum(start, end, album);
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getSalesByAlbumContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        double previousFinal = 95.76;
        double albumFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, albumFinal);

        // Action
        List<Object[]> list = reports.getSalesByAlbumTotals(start, end, album);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal + albumFinal, Offset.offset(0.001));
    }

    @Test
    public void getSalesByAlbumContainsNotTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        double previousFinal = 95.76;
        double albumFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);

        createNewInvoiceAlbum(invoice, album, albumFinal);

        // Action
        List<Object[]> list = reports.getSalesByAlbumTotals(start, end, album);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal, Offset.offset(0.001));
    }

    @Test
    public void getSalesByTrackDetailsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(1);
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByTrack(start, end, track);
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getSalesByTrackDetailsNotContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(1);
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByTrack(start, end, track);
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getSalesByTrackContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        double previousFinal = 2.38;
        double trackFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(3);
        createNewInvoiceTrack(invoice, track, trackFinal);

        // Action
        List<Object[]> list = reports.getSalesByTrackTotals(start, end, track);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal + trackFinal, Offset.offset(0.001));
    }

    @Test
    public void getSalesByTrackNotContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/02/15");
        double previousFinal = 2.38;
        double trackFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(3);
        createNewInvoiceTrack(invoice, track, trackFinal);

        // Action
        List<Object[]> list = reports.getSalesByTrackTotals(start, end, track);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal, Offset.offset(0.001));
    }

    @Test
    public void getSalesByArtistAlbumContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        double previousFinal = 95.76;
        double albumFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, albumFinal);

        // Action
        List<Object[]> list = reports.getSalesByArtistAlbumsTotals(start, end, artistJpa.findArtist(28));
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(albumFinal + previousFinal, Offset.offset(0.001));
    }

    @Test
    public void getSalesByArtistAlbumsContainsNotTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        double previousFinal = 95.76;
        double albumFinal = 10.00;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, albumFinal);

        // Action
        List<Object[]> list = reports.getSalesByArtistAlbumsTotals(start, end, artistJpa.findArtist(28));
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal, Offset.offset(0.001));
    }

    @Test
    public void getSalesByArtistAlbumDetailsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByArtistAlbums(start, end, artistJpa.findArtist(28));
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getSalesByArtistAlbumDetailsContainsNotTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByArtistAlbums(start, end, artistJpa.findArtist(28));
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getSalesByArtistTrackDetailsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(3);
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByArtistTracks(start, end, artistJpa.findArtist(27));
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getSalesByArtistTrackDetailsNotContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(3);
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByArtistTracks(start, end, artistJpa.findArtist(27));
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getSalesByArtistTrackContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        double previousFinal = 48.830000000000005;
        double trackFinal = 10;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(3);
        createNewInvoiceTrack(invoice, track, trackFinal);

        // Action
        List<Object[]> list = reports.getSalesByArtistTracksTotals(start, end, artistJpa.findArtist(27));
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(trackFinal + previousFinal, Offset.offset(0.001));
    }

    @Test
    public void getSalesByArtistTrackNotContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/02/15");
        double previousFinal = 48.830000000000005;
        double trackFinal = 10;
        ShopUser test = createTestUser();
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(3);
        createNewInvoiceTrack(invoice, track, trackFinal);

        // Action
        List<Object[]> list = reports.getSalesByArtistTracksTotals(start, end, artistJpa.findArtist(27));
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal, Offset.offset(0.001));;
    }

    @Test
    public void getSalesByClientAlbumContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        double previousFinal = 38.91;
        double albumFinal = 10.00;
        ShopUser test = userJpa.findShopUser(586);
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, albumFinal);

        // Action
        List<Object[]> list = reports.getSalesByClientAlbumsTotals(start, end, test);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(albumFinal + previousFinal, Offset.offset(0.001));;
    }

    @Test
    public void getSalesByClientAlbumsNotContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        double previousFinal = 38.91;
        double albumFinal = 10.00;
        ShopUser test = userJpa.findShopUser(586);
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, albumFinal);

        // Action
        List<Object[]> list = reports.getSalesByClientAlbumsTotals(start, end, test);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal, Offset.offset(0.001));;
    }

    @Test
    public void getSalesByClientAlbumDetailsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = userJpa.findShopUser(586);
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByClientAlbums(start, end, test);
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getSalesByClientAlbumDetailsContainsNotTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = userJpa.findShopUser(586);
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Album album = albumJpa.findAlbum(1);
        createNewInvoiceAlbum(invoice, album, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByClientAlbums(start, end, test);
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getSalesByClientTrackDetailsContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        ShopUser test = userJpa.findShopUser(244);
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(1);
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByClientTracks(start, end, test);
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isTrue();
    }

    @Test
    public void getSalesByClientTrackDetailsNotContainsTest() throws SQLException, Exception {
        // Set Up
        boolean isFound = false;
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date outside start and end
        Date saleDate = format.parse("2017/02/15");
        ShopUser test = userJpa.findShopUser(244);
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(1);
        createNewInvoiceTrack(invoice, track, 10.00);

        // Action
        List<Object[]> list = reports.getSalesByClientTracks(start, end, test);
        // obj[0] has invoice
        for (Object[] obj : list) {
            if (obj[0].equals(invoice)) {
                isFound = true;
                break;
            }
        }

        // Assert
        assertThat(isFound).isFalse();
    }

    @Test
    public void getSalesByClientTrackContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/01/15");
        double previousFinal = 7.34;
        double trackFinal = 10.00;
        ShopUser test = userJpa.findShopUser(244);
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(3);
        createNewInvoiceTrack(invoice, track, trackFinal);

        // Action
        List<Object[]> list = reports.getSalesByClientTracksTotals(start, end, test);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(trackFinal + previousFinal, Offset.offset(0.001));;
    }

    @Test
    public void getSalesByClientTrackNotContainsTest() throws SQLException, Exception {
        // Set Up
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date start = format.parse("2017/01/01");
        Date end = format.parse("2017/02/01");
        // Sale date between start and end
        Date saleDate = format.parse("2017/02/15");
        double previousFinal = 7.34;
        double trackFinal = 10.00;
        ShopUser test = userJpa.findShopUser(244);
        Invoice invoice = createNewInvoice(saleDate, 10, 11, test);
        Track track = trackJpa.findTrack(3);
        createNewInvoiceTrack(invoice, track, trackFinal);

        // Action
        List<Object[]> list = reports.getSalesByClientTracksTotals(start, end, test);
        // First item in list contains data, first item in array contains sum of final costs
        Object[] array = list.get(0);
        double totalFinal = (double) array[0];

        // Assert
        assertThat(totalFinal).isCloseTo(previousFinal, Offset.offset(0.001));;
    }

    /**
     * Test user never has to be different, do not need to customize.
     *
     * @return the user
     */
    private ShopUser createTestUser() throws Exception {
        ShopUser user = new ShopUser();

        user.setTitle("Mr");
        user.setLastName("Marley");
        user.setFirstName("Bob");
        user.setStreetAddress("cats avenue");
        user.setCity("catcity");
        user.setCountry("Canada");
        user.setPostalCode("A1A1A1");
        user.setHomePhone("1234567890");
        user.setEmail("bob@cat.com");
        user.setHashedPw("kitty".getBytes());
        user.setSalt("cat");
        user.setProvinceId(provinceJpa.findProvinceEntities().get(0));
        userJpa.create(user);

        return user;
    }

    /**
     * Invoices must be customizable, as dates can vary.
     *
     * @param saleDate Sale date of invoice
     * @param totalNetValue net value of invoice
     * @param totalGrossValue gross value of invoice
     * @param user invoice's user
     * @return the invoice
     */
    private Invoice createNewInvoice(Date saleDate, double totalNetValue,
            double totalGrossValue, ShopUser user) throws Exception {
        Invoice invoice = new Invoice();

        invoice.setSaleDate(saleDate);
        invoice.setTotalNetValue(totalNetValue);
        invoice.setTotalGrossValue(totalGrossValue);
        invoice.setUserId(user);
        invoiceJpa.create(invoice);

        return invoice;
    }

    /**
     * Test track never has to be different, do not need to customize.
     *
     * @return the test track
     * @throws Exception
     */
    private Track createTestTrack() throws Exception {
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Track t = new Track();

        t.setTitle("Title");
        t.setReleaseDate(format.parse("2016/12/31"));
        t.setPlayLength("3:00");
        t.setAlbumTrackNumber(1);
        t.setPartOfAlbum((short) 1);
        t.setCostPrice(5.0);
        t.setListPrice(6.0);
        t.setAlbumId(albumJpa.findAlbum(1));
        t.setArtistId(artistJpa.findArtist(1));
        t.setCoverArtId(coverJpa.findCoverArt(1));
        t.setGenreId(genreJpa.findGenre(1));
        t.setSongwriterId(songwriterJpa.findSongwriter(1));
        t.setDateEntered(Calendar.getInstance().getTime());
        trackJpa.create(t);

        return t;
    }

    /**
     * Invoice Tracks must be customizable, as ids and final price are variable.
     *
     * @param invoiceId
     * @param trackId
     * @param finalPrice
     * @return the invoice track
     * @throws Exception
     */
    private void createNewInvoiceTrack(Invoice invoiceId, Track trackId,
            double finalPrice) throws Exception {
        InvoiceTrack it = new InvoiceTrack();
        InvoiceTrackPK itpk = new InvoiceTrackPK();

        itpk.setInvoiceId(invoiceId.getId());
        itpk.setTrackId(trackId.getId());

        it.setInvoiceTrackPK(itpk);
        it.setFinalPrice(finalPrice);
        it.setInvoice(invoiceId);
        it.setTrack(trackId);
        invoiceTrackJpa.create(it);
    }

    private Album createTestAlbum() throws Exception {
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Album a = new Album();

        a.setTitle("Title");
        a.setReleaseDate(format.parse("2016/12/31"));
        a.setNumTracks(1);
        a.setDateEntered(Calendar.getInstance().getTime());
        a.setCostPrice(5.0);
        a.setListPrice(6.0);
        a.setArtistId(artistJpa.findArtist(1));
        a.setCoverArtId(coverJpa.findCoverArt(1));
        a.setGenreId(genreJpa.findGenre(1));
        a.setRecordingLabelId(recordingJpa.findRecordingLabel(1));
        albumJpa.create(a);

        return a;
    }

    /**
     * Invoice Albums must be customizable, as ids and final price are variable.
     *
     * @param invoiceId
     * @param trackId
     * @param finalPrice
     * @return the invoice track
     * @throws Exception
     */
    private void createNewInvoiceAlbum(Invoice invoiceId, Album albumId,
            double finalPrice) throws Exception {
        InvoiceAlbum ia = new InvoiceAlbum();
        InvoiceAlbumPK iapk = new InvoiceAlbumPK();

        iapk.setInvoiceId(invoiceId.getId());
        iapk.setAlbumId(albumId.getId());

        ia.setInvoiceAlbumPK(iapk);
        ia.setFinalPrice(finalPrice);
        ia.setInvoice(invoiceId);
        ia.setAlbum(albumId);
        invoiceAlbumJpa.create(ia);
    }
}

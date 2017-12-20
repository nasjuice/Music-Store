
package com.pandamedia.arquillian.test;

/**
 *
 * @author Naasir Jusab
 */


import com.pandamedia.beans.ManagerSurveyBean;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import persistence.controllers.FrontPageSettingsJpaController;
import persistence.controllers.ShopUserJpaController;
import persistence.controllers.SurveyJpaController;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.FrontPageSettings;
import persistence.entities.Survey;
import persistence.entities.Track;

@RunWith(Arquillian.class)
public class ManagerSurveyArq {
    
    @Resource(name = "java:app/jdbc/pandamedialocal")
    private DataSource ds;
    @Inject
    private ManagerSurveyBean managerSurveyBacking;
    @Inject
    private FrontPageSettingsJpaController fpsController;
    @Inject
    private SurveyJpaController surveyController;
    
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
    public void testSave()
    {
        Survey survey = new Survey();
        survey.setQuestion("Why is JSF amazing?");
        survey.setAnswerA("Because it is amazing");
        survey.setAnswerB("Because Java is amazing");
        survey.setAnswerC("Because beans are amazing");
        survey.setAnswerD("No it is not amazing");
        survey.setVotesA(0);
        survey.setVotesB(0);
        survey.setVotesC(0);
        survey.setVotesD(0);
        managerSurveyBacking.setSurvey(survey);
        managerSurveyBacking.save();
        
        List<Survey> list = managerSurveyBacking.getAll();
        assertThat(list.get(list.size()-1)).isEqualTo(surveyController.findSurvey(list.get(list.size()-1).getId()));
            
    }
    
    @Test
    public void testRemove()
    {   
        Survey survey = new Survey();
        survey.setQuestion("Why is JSF so fun?");
        survey.setAnswerA("Because it is fun");
        survey.setAnswerB("Because it is Java");
        survey.setAnswerC("Because beans are the way to go");
        survey.setAnswerD("No it is not fun");
        survey.setVotesA(0);
        survey.setVotesB(0);
        survey.setVotesC(0);
        survey.setVotesD(0);
        managerSurveyBacking.setSurvey(survey);
        managerSurveyBacking.save();
        
        List<Survey> listBefore = managerSurveyBacking.getAll();
        //id of the object added
        Integer surveyId = listBefore.get(listBefore.size()-1).getId();
        managerSurveyBacking.remove(listBefore.get(listBefore.size()-1).getId());
        List<Survey> listAfter = managerSurveyBacking.getAll();
        
        boolean isRemoved = true;
        for(Survey surveyInList:listAfter)
        {
            if(surveyInList.getId() == surveyId)
                isRemoved=false;
        }
        
        
        assertThat(isRemoved).isTrue();
    }
    
    @Test
    public void testSelect()
    {
        managerSurveyBacking.select(1);
        FrontPageSettings fps = fpsController.findFrontPageSettings(1);
        Survey survey = managerSurveyBacking.findSurveyById(1);
        
        assertThat(fps.getSurveyId()).isEqualTo(survey);
        
    }
    
}

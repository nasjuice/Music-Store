package com.pandamedia.selenium.tests;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Evan Glicakis
 */
public class ReviewTestIT {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp() {
        ChromeDriverManager.getInstance().setup();
        driver = new ChromeDriver();
    }

//    @After
//    public void destroy(){
//        driver.quit();
//    }
    @Test
    public void testGoToWriteAReview() {
        driver.get("http://localhost:8080/pandamedia/");
        wait = new WebDriverWait(driver, 10);
        driver.findElement(By.id("browse-album-caption")).click();
        wait = new WebDriverWait(driver, 10);
        driver.findElement(By.xpath("//div[contains(@id, 'popular-browse')]/*[1]")).click();
        wait = new WebDriverWait(driver, 10);
//       driver.findElement(By.xpath("//*[@id=\"style-1\"]/tr[1]/td[3]/form/button")).click();
        driver.findElement(By.xpath("//*[@id=\"style-1\"]/tr[1]/td[1]/form")).click();
         wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d){
                boolean isValid = false;
                // we should be on the track page. The track page contains a 
                // comment section that is unique to the entire website
                // therefore we will be using this to make sure that we are on
                // the track page.
                isValid = driver.findElement(By.id("comment-section")).isDisplayed();
                return isValid;             
            }
          });
        driver.quit();

    }
    /**
     * This test case doesnt work as I can't figure out how to select an element inside the div that was populated
     * through ajax. All the documentation online says a wait will fix the issue,
     * however it hasn't solved anything for me. So I'll just ignore this test for now.
     */
    @Ignore
    @Test
    public void testAddPunkAlbumToCart() {
        driver.get("http://localhost:8080/pandamedia/");
        wait = new WebDriverWait(driver, 10);
        driver.findElement(By.id("browse-album-caption")).click();
        wait = new WebDriverWait(driver, 10);
        driver.findElement(By.xpath("//*[@id=\"j_idt79:j_idt80:2:j_idt81\"]/h4")).click();

    }
}

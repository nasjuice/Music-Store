
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
 * @author Naasir Jusab
 */
public class ManagementTestIT {
    
    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp()
    {
        ChromeDriverManager.getInstance().setup();
        driver = new ChromeDriver();
        
        driver.get("http://localhost:8080/pandamedia/userconnection/login.xhtml");
        
        wait = new WebDriverWait(driver,10);
        
        driver.findElement(By.id("loginForm:emailInput")).sendKeys("m@m.com");
        driver.findElement(By.id("loginForm:passwordInput")).sendKeys("m");
        driver.findElement(By.id("loginForm:loginBtn")).click();
    }
    
//    @After
//    public void destroy(){
//        driver.quit();
//    }
    
    @Test
    public void testApproveReviews() throws Exception 
    {

       
        driver.get("http://localhost:8080/pandamedia/manager/reviews.xhtml");
        wait = new WebDriverWait(driver,10);
        
        //test the approve btn
        driver.findElement(By.id("reviewFormID:reviewTableID:0:approveBtn")).click();

        driver.findElement(By.id("reviewFormID:reviewTableID:0:approveBtn")).click();
        
        wait = new WebDriverWait(driver,10);
        
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d)
            {
                return d.findElement(By.id("reviewFormID:reviewTableID:0:approvalStatusText")).getText().contains("1");
            }


        });
        
        driver.quit();   
    }

    @Test
    public void testDisapproveReviews() throws Exception 
    {
       
        driver.get("http://localhost:8080/pandamedia/manager/reviews.xhtml");
        
        
        wait = new WebDriverWait(driver,10);

        
        //test the disapprove btn
        driver.findElement(By.id("reviewFormID:reviewTableID:0:disapproveBtn")).click();
        

        driver.findElement(By.id("reviewFormID:reviewTableID:0:disapproveBtn")).click();
        
        wait = new WebDriverWait(driver,10);
        
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d)
            {
                return d.findElement(By.id("reviewFormID:reviewTableID:0:approvalStatusText")).getText().contains("0");
            }


        });
        
        driver.quit();       
        
    }

    @Test
    public void testEditAlbum() throws Exception
    {
   
        driver.get("http://localhost:8080/pandamedia/manager/manager_index.xhtml");
        
        
        wait = new WebDriverWait(driver,10);
        
          //test the create track btn
        driver.findElement(By.id("albumFormID:albumTable:0:editAlbumBtn")).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='editAlbumForm:title']"))).clear();
        driver.findElement(By.xpath("//input[@id='editAlbumForm:title']")).clear();
        driver.findElement(By.id("editAlbumForm:title")).clear();
        driver.findElement(By.id("editAlbumForm:title")).sendKeys("Evan sucks");
//        
        driver.findElement(By.id("editAlbumForm:releaseDate")).clear();
        driver.findElement(By.id("editAlbumForm:releaseDate")).sendKeys("1/1/1900");
//        
        driver.findElement(By.id("editAlbumForm:numTracks")).clear();
        driver.findElement(By.id("editAlbumForm:numTracks")).sendKeys("12");
//        
        driver.findElement(By.id("editAlbumForm:dateEntered")).clear();
        driver.findElement(By.id("editAlbumForm:dateEntered")).sendKeys("1/1/1900");
//        
        driver.findElement(By.id("editAlbumForm:costPrice")).clear();
        driver.findElement(By.id("editAlbumForm:costPrice")).sendKeys("1.00");
//        
        driver.findElement(By.id("editAlbumForm:listPrice")).clear();
        driver.findElement(By.id("editAlbumForm:listPrice")).sendKeys("1.50");
//        
        driver.findElement(By.id("editAlbumForm:salePriceAlbum")).clear();
        driver.findElement(By.id("editAlbumForm:salePriceAlbum")).sendKeys("1.00");
//        
        driver.findElement(By.id("editAlbumForm:removalStatus")).clear();
        driver.findElement(By.id("editAlbumForm:removalStatus")).sendKeys("0");
//         
        driver.findElement(By.id("editAlbumForm:removalDate")).clear();
//        
         driver.findElement(By.id("editAlbumForm:editAlbumBtn")).click();
               
          wait = new WebDriverWait(driver,10);
        
          wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d)
            {
                boolean isValid = false;
                isValid = d.findElement(By.id("albumFormID:albumTable:0:title")).getText().contains("Evan sucks");
                isValid = d.findElement(By.id("albumFormID:albumTable:0:releaseDate")).getText().contains("01/01/1900");
                isValid = d.findElement(By.id("albumFormID:albumTable:0:numTracks")).getText().contains("12");
                isValid = d.findElement(By.id("albumFormID:albumTable:0:dateEntered")).getText().contains("01/01/1900");
                isValid = d.findElement(By.id("albumFormID:albumTable:0:costPrice")).getText().contains("1.0");
                
                isValid = d.findElement(By.id("albumFormID:albumTable:0:listPrice")).getText().contains("1.5");
                isValid = d.findElement(By.id("albumFormID:albumTable:0:salePrice")).getText().contains("1.0");
                isValid = d.findElement(By.id("albumFormID:albumTable:0:removalStatus")).getText().contains("0");

                return isValid;             
            }


          });
        
          driver.quit();            
    }
    
    @Test
    public void testRemovalAlbum() throws Exception 
    {

        driver.get("http://localhost:8080/pandamedia/manager/manager_index.xhtml");

        wait = new WebDriverWait(driver,10);
        
        //test the disapprove btn
        driver.findElement(By.id("albumFormID:albumTable:0:removeAlbumBtn")).click();

        driver.findElement(By.id("albumFormID:albumTable:0:removeAlbumBtn")).click();
        
        wait = new WebDriverWait(driver,10);
        
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d)
            {
                 boolean isValid = false;
                 isValid = d.findElement(By.id("albumFormID:albumTable:0:removalStatus")).getText().contains("1");
                 
                 return isValid;
            }


        });
        
        driver.quit();       
        
    }
    
    
}

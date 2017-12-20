package com.pandamedia.selenium.tests;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Pierre Azelart
 */
public class SearchTestIT {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp() {
        ChromeDriverManager.getInstance().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, 10);
    }

    @Test
    public void testSimpleSearch() throws Exception {
        /*ChromeDriverManager.getInstance().setup();

        driver = new ChromeDriver();
        driver.get("http://localhost:8080/pandamedia/reviews.xhtml");

        //test the approve btn
        driver.findElement(By.xpath("/form[@id=reviewFormID]/column[@id=options]/commandButton[id=approveBtn]"));

        //delete this when the bug is fixed
        driver.findElement(By.xpath("/form[@id=reviewFormID]/column[@id=options]/commandButton[id=approveBtn]"));

        wait = new WebDriverWait(driver, 10);

        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return d.findElement(By.xpath("/form[@id=reviewFormID]/column[@id=approvalStatusID]/outputText[id=approvalStatusText]")).getText().contains("1");
            }

        });*/
        
        
        ChromeDriverManager.getInstance().setup();
        driver = new ChromeDriver();
        driver.get("http://localhost:8080/pandamedia/shop/mainpage.xhtml");
        
        //Tests the search
        driver.findElement(By.id("typeDropdown")).click();
        wait = new WebDriverWait(driver, 2);
        
        /*driver.findElement(By.xpath("/form[@id=menuForm]/ul[@id=type-nav]/li[@id=type-dropdown]/ul[@id=dropdown-menuId]/li[@id=albumLi]")).click();
        wait = new WebDriverWait(driver, 2);*/
        
        driver.findElement(By.id("menuForm:albumButton")).click();
        wait = new WebDriverWait(driver, 1);
        
        
        driver.findElement(By.id("menuForm:formInput")).sendKeys("cha\n");
        wait = new WebDriverWait(driver, 1);
        
        driver.findElement(By.id("menuForm:searchButton")).click();
        wait = new WebDriverWait(driver, 1);
        

        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return d.findElement(By.tagName("body")).getText().contains("404");
            }
        });
        driver.quit();
    }

    /*@Test
    public void testDisapprove() throws Exception {
        /*ChromeDriverManager.getInstance().setup();

        driver = new ChromeDriver();
        driver.get("http://localhost:8080/pandamedia/reviews.xhtml");

        //test the approve btn
        driver.findElement(By.xpath("/form[@id=reviewFormID]/column[@id=options]/commandButton[id=disapproveBtn]"));

        //delete this when the bug is fixed
        driver.findElement(By.xpath("/form[@id=reviewFormID]/column[@id=options]/commandButton[id=disapproveBtn]"));

        wait = new WebDriverWait(driver, 10);

        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return d.findElement(By.xpath("/form[@id=reviewFormID]/column[@id=approvalStatusID]/outputText[id=approvalStatusText]")).getText().contains("0");
            }

        });

        driver.quit();
        ChromeDriverManager.getInstance().setup();

        driver = new ChromeDriver();
        driver.get("http://localhost:8080/pandamedia/shop/mainpage.xhtml");

    }*/

}

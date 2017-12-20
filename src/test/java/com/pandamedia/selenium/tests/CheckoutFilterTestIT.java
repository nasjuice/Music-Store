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
public class CheckoutFilterTestIT {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp() {
        ChromeDriverManager.getInstance().setup();
        driver = new ChromeDriver();
    }
    
    @Test
    public void testCheckoutFilter(){
        driver.get("http://localhost:8080/pandamedia/");
        wait = new WebDriverWait(driver, 10);
        driver.findElement(By.id("browse-album-caption")).click();
        wait = new WebDriverWait(driver, 10);
        driver.findElement(By.xpath("//div[contains(@id, 'popular-browse')]/*[1]")).click();
        wait = new WebDriverWait(driver, 10);
        driver.findElement(By.xpath("//*[@id=\"style-1\"]/tr[1]/td[3]/form/button")).click();
        driver.get("http://localhost:8080/pandamedia/shop/cart.xhtml");
        driver.findElement(By.id("checkoutForm:checkoutBtn")).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginForm:emailInput"))).sendKeys("e@e.com");
        
        driver.findElement(By.id("loginForm:passwordInput")).sendKeys("e");
        driver.findElement(By.id("loginForm:loginBtn")).click();
        
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d){
                boolean isValid = false;
                // we should return back to the checkout page, to ensure that we have arrived at the checkout page, after logging in
                // check if the page contains the checkout button.
                isValid = driver.findElement(By.id("checkoutForm:checkoutBtn")).isDisplayed();
                return isValid;             
            }
          });
        driver.quit();
    }

}

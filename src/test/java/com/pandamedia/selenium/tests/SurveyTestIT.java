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
 * Test not working, selenium cannot seem to be able to find the radio 
 * buttons of the survey.
 * @author Hau Gilles Che
 */
public class SurveyTestIT {
    private WebDriver driver;
    private WebDriverWait wait;
    
    @Before
    public  void setUp(){
        ChromeDriverManager.getInstance().setup();
        driver = new ChromeDriver();
    }
    
    @Test
    public void testSurvey() throws Exception{
        driver.get("http://localhost:8080/pandamedia/shop/mainpage.xhtml");
        wait=new WebDriverWait(driver,10);
        
        driver.findElement(By.id("browse-survey")).click();
        driver.findElement(By.id("j_idt96:submitBtn")).click();
        wait = new WebDriverWait(driver,10);
        wait.until(new ExpectedCondition<Boolean>(){
            @Override
            public Boolean apply(WebDriver d){
                return d.findElement(By.id("resultsHeader")).getText().contains("Results");
            }
        });
    }
}

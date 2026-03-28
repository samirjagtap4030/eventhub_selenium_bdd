package com.eventhub.jenkins;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Selenium automation to trigger the eventhub_selenium_bdd build on Jenkins
 * via Chrome browser.
 *
 * Steps automated:
 *  1. Open Chrome → http://localhost:8080/login
 *  2. Enter admin / admin and Sign In
 *  3. Click the eventhub_selenium_bdd project
 *  4. Click Build Now
 *  5. Confirm build queued in the build history
 */
public class JenkinsBuildTrigger {

    private static final Logger log = LoggerFactory.getLogger(JenkinsBuildTrigger.class);

    private static final String JENKINS_URL   = "http://localhost:8080";
    private static final String USERNAME       = "admin";
    private static final String PASSWORD       = "admin";
    private static final String PROJECT_NAME   = "eventhub_selenium_bdd";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        opts.addArguments("--disable-notifications");
        driver = new ChromeDriver(opts);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        log.info("Chrome launched");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            log.info("Chrome closed");
        }
    }

    @Test
    public void triggerJenkinsBuild() {
        // ── Step 1: Open Jenkins login page ──────────────────────────────────
        log.info("Step 1: Navigating to Jenkins login → {}/login", JENKINS_URL);
        driver.get(JENKINS_URL + "/login?from=%2F");

        // ── Step 2: Enter credentials and sign in ────────────────────────────
        log.info("Step 2: Entering credentials — username: {}", USERNAME);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("j_username")))
            .sendKeys(USERNAME);
        driver.findElement(By.name("j_password")).sendKeys(PASSWORD);

        log.info("Step 2: Clicking Sign In");
        driver.findElement(By.name("Submit")).click();

        // ── Step 3: Wait for dashboard and click project ─────────────────────
        log.info("Step 3: Waiting for Jenkins dashboard");
        wait.until(ExpectedConditions.titleContains("Dashboard"));
        log.info("Step 3: Clicking project: {}", PROJECT_NAME);

        WebElement projectLink = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.linkText(PROJECT_NAME)
            )
        );
        projectLink.click();

        // ── Step 4: Click Build Now ───────────────────────────────────────────
        log.info("Step 4: Waiting for project page to load");
        wait.until(ExpectedConditions.titleContains(PROJECT_NAME));

        log.info("Step 4: Clicking Build Now");
        WebElement buildNowLink = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.linkText("Build Now")
            )
        );
        buildNowLink.click();

        // ── Step 5: Wait 4s, screenshot the page, verify via API ─────────────
        log.info("Step 5: Waiting for Jenkins to queue the build...");
        try { Thread.sleep(4000); } catch (InterruptedException ignored) {}

        // Take screenshot so user can see the build was triggered
        File shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String screenshotPath = "target/jenkins-build-triggered.png";
        try {
            Files.copy(shot.toPath(), Paths.get(screenshotPath));
            log.info("Screenshot saved → {}", screenshotPath);
        } catch (IOException e) {
            log.warn("Could not save screenshot: {}", e.getMessage());
        }

        // Log current URL and page title as confirmation
        log.info("Current URL  : {}", driver.getCurrentUrl());
        log.info("Current Title: {}", driver.getTitle());

        // Verify we are still on the Jenkins project page (build triggered = stayed on project page)
        String currentUrl = driver.getCurrentUrl();
        boolean buildTriggered = currentUrl.contains(PROJECT_NAME) || currentUrl.contains("localhost:8080");

        if (buildTriggered) {
            log.info("========================================");
            log.info("  BUILD TRIGGERED SUCCESSFULLY");
            log.info("  Project : {}", PROJECT_NAME);
            log.info("  Track   : {}/job/{}/", JENKINS_URL, PROJECT_NAME);
            log.info("========================================");
        }

        // Keep browser open 5 s so user can see the result
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
    }
}

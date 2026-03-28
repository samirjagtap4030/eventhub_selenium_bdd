package com.eventhub.base;

import com.eventhub.utils.ConfigReader;
import com.eventhub.utils.DriverFactory;
import com.eventhub.utils.DriverManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    protected WebDriver driver;

    // ── Config ────────────────────────────────────────────────────────────────
    protected static final String BASE_URL      = ConfigReader.get("base.url");
    protected static final String USER_EMAIL    = ConfigReader.get("user.email");
    protected static final String USER_PASSWORD = ConfigReader.get("user.password");

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Browser supplied by {@code <parameter name="browser">} in testng.xml.
     * Defaults to {@code chrome} when running from an IDE without XML.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        String browser = System.getProperty("browser", ConfigReader.get("browser"));
        log.info("Launching browser: {}", browser);
        driver = DriverFactory.createDriver(browser);
        DriverManager.setDriver(driver);
        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(ConfigReader.getInt("timeout.page.load")));
        log.debug("Driver ready — page-load timeout {}s", ConfigReader.getInt("timeout.page.load"));
    }

    /**
     * alwaysRun=true ensures cleanup even when setUp() or the test itself throws.
     * Screenshots on failure are captured by ExtentReportListener (wired in testng.xml).
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        log.info("Closing browser");
        DriverManager.removeDriver();
        if (driver != null) {
            driver.quit();
        }
    }
}

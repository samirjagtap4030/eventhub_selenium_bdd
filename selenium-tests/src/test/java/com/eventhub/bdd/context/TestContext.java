package com.eventhub.bdd.context;

import com.eventhub.utils.ConfigReader;
import com.eventhub.utils.DriverFactory;
import com.eventhub.utils.DriverManager;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

/**
 * Shared scenario state — one instance per scenario via PicoContainer.
 *
 * <p>PicoContainer creates this object once per scenario and injects it into
 * every step-definition class and the hooks class that declares it as a
 * constructor parameter.  All collaborators therefore share the same
 * {@code driver} and {@code bookingRef} without any static state.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@link com.eventhub.bdd.hooks.CucumberHooks#setUp()} calls
 *       {@link #initDriver()} — browser is ready before Background steps run.</li>
 *   <li>Step definitions read/write {@link #bookingRef}.</li>
 *   <li>{@link com.eventhub.bdd.hooks.CucumberHooks#tearDown()} calls
 *       {@link #quitDriver()} — browser closes after each scenario.</li>
 * </ol>
 */
public class TestContext {

    // ── Config constants (loaded once from config.properties / env vars) ────
    public final String baseUrl      = ConfigReader.get("base.url");
    public final String userEmail    = ConfigReader.get("user.email");
    public final String userPassword = ConfigReader.get("user.password");

    // ── Scenario-scoped mutable state ────────────────────────────────────────
    /** Booking reference captured after confirming a booking in the Background. */
    public String bookingRef;

    private WebDriver driver;

    // ── Driver lifecycle ─────────────────────────────────────────────────────

    /**
     * Creates a WebDriver instance using the browser specified by the
     * {@code browser} system property (falls back to {@code config.properties}).
     * Also registers the driver in {@link DriverManager} so that
     * {@link com.eventhub.listeners.ExtentReportListener} can capture screenshots.
     */
    public void initDriver() {
        String browser = System.getProperty("browser", ConfigReader.get("browser"));
        driver = DriverFactory.createDriver(browser);
        DriverManager.setDriver(driver);
        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(ConfigReader.getInt("timeout.page.load")));
    }

    /** Returns the WebDriver for this scenario. */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Removes the driver from {@link DriverManager} and quits the browser.
     * Safe to call even if {@link #initDriver()} was never called.
     */
    public void quitDriver() {
        if (driver != null) {
            DriverManager.removeDriver();
            driver.quit();
            driver = null;
        }
    }
}

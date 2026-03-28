package com.eventhub.bdd.hooks;

import com.eventhub.bdd.context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cucumber lifecycle hooks — browser setup and teardown around each scenario.
 *
 * <p>{@link TestContext} is injected by PicoContainer, guaranteeing the same
 * instance is shared with all step-definition classes in the same scenario.
 *
 * <p>Execution order relative to scenario steps:
 * <pre>
 *   {@code @Before} setUp()    ← browser launched
 *   Background steps
 *   Scenario steps
 *   {@code @After}  tearDown() ← browser closed (always, even on failure)
 * </pre>
 */
public class CucumberHooks {

    private static final Logger log = LoggerFactory.getLogger(CucumberHooks.class);

    private final TestContext ctx;

    public CucumberHooks(TestContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Launches the browser before each scenario.
     * The {@code @Before} order defaults to 1000; no explicit order needed here
     * since this is the only hook class.
     */
    @Before
    public void setUp(Scenario scenario) {
        log.info("=== @Before: launching browser for scenario: [{}] ===", scenario.getName());
        ctx.initDriver();
    }

    /**
     * Closes the browser after each scenario.
     * On failure, captures a screenshot and:
     * <ul>
     *   <li>Attaches it to the Cucumber scenario (embedded in HTML report)</li>
     *   <li>Saves it to {@code target/screenshots/} for Jenkins artifact archiving</li>
     * </ul>
     */
    @After
    public void tearDown(Scenario scenario) {
        log.info("=== @After: closing browser — scenario [{}] status: {} ===",
                scenario.getName(), scenario.getStatus());

        if (scenario.isFailed()) {
            captureScreenshot(scenario);
        }

        ctx.quitDriver();
    }

    private void captureScreenshot(Scenario scenario) {
        WebDriver driver = ctx.getDriver();
        if (!(driver instanceof TakesScreenshot)) {
            log.warn("Driver does not support screenshots");
            return;
        }

        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // Embed in Cucumber HTML report
            scenario.attach(screenshot, "image/png", scenario.getName() + " — FAILED");

            // Save to filesystem so Jenkins can archive it
            String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
            Path screenshotsDir = Paths.get("target", "screenshots");
            Files.createDirectories(screenshotsDir);
            Path dest = screenshotsDir.resolve(safeName + ".png");
            Files.write(dest, screenshot);
            log.info("Screenshot saved to: {}", dest.toAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to save screenshot for scenario [{}]: {}", scenario.getName(), e.getMessage());
        }
    }
}

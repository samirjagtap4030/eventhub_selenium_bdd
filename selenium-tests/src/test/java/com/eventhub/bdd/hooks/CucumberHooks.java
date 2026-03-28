package com.eventhub.bdd.hooks;

import com.eventhub.bdd.context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * {@code alwaysRun} semantics are implicit — Cucumber {@code @After} always
     * executes regardless of scenario outcome.
     */
    @After
    public void tearDown(Scenario scenario) {
        log.info("=== @After: closing browser — scenario [{}] status: {} ===",
                scenario.getName(), scenario.getStatus());
        ctx.quitDriver();
    }
}

package com.eventhub.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * TestNG entry point for the Cucumber BDD suite.
 *
 * <p>{@link AbstractTestNGCucumberTests} exposes each Gherkin scenario as a
 * TestNG {@code @Test} row via {@code @DataProvider}, so TestNG listeners
 * (e.g. ExtentReportListener) and Surefire reporting work out of the box.
 *
 * <h3>Tag filtering</h3>
 * Default: {@code @regression} (all three scenarios).
 * Override at runtime with the system property:
 * <pre>
 *   mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags="@smoke"
 * </pre>
 *
 * <h3>Reports generated</h3>
 * <ul>
 *   <li>{@code target/cucumber-reports/index.html} — Cucumber HTML report</li>
 *   <li>{@code target/cucumber-reports/cucumber.json} — JSON for CI dashboards</li>
 *   <li>{@code target/extent-reports/TestReport.html} — Extent report via listener</li>
 * </ul>
 */
@CucumberOptions(
        features = "classpath:features",
        glue     = {
            "com.eventhub.bdd.steps",
            "com.eventhub.bdd.hooks"
        },
        plugin   = {
            "pretty",
            "html:target/cucumber-reports/index.html",
            "json:target/cucumber-reports/cucumber.json"
        },
        tags     = "@regression",
        monochrome = true
)
public class CucumberRunner extends AbstractTestNGCucumberTests {

    /**
     * Runs scenarios sequentially (parallel = false) to match the TestNG suite
     * configuration and avoid cross-scenario browser state conflicts.
     */
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}

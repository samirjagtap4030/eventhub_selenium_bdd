package com.eventhub.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.eventhub.utils.ConfigReader;
import com.eventhub.utils.DriverManager;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener that produces an Extent Spark HTML report.
 * Wired via {@code <listeners>} in testng.xml so it runs in every context
 * (IDE, Maven, CI) without code changes.
 *
 * <ul>
 *   <li>PASSED  → green pass entry</li>
 *   <li>FAILED  → red fail entry + inline Base64 screenshot</li>
 *   <li>SKIPPED → orange skip entry</li>
 * </ul>
 *
 * Report is written to {@code target/extent-reports/TestReport.html}.
 */
public class ExtentReportListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(ExtentReportListener.class);

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // ── Suite lifecycle ───────────────────────────────────────────────────────

    @Override
    public synchronized void onStart(ITestContext context) {
        String reportPath = "target/extent-reports/TestReport.html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setReportName("EventHub E2E Test Report");
        spark.config().setDocumentTitle("Booking Management Tests");
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Application", ConfigReader.get("base.url"));
        extent.setSystemInfo("Browser",
                context.getCurrentXmlTest().getParameter("browser") != null
                        ? context.getCurrentXmlTest().getParameter("browser")
                        : "chrome");
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
    }

    @Override
    public synchronized void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
            log.info("Extent Report saved → target/extent-reports/TestReport.html");
        }
    }

    // ── Test lifecycle ────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        // Use @Test description as the display name; fall back to method name
        String name = result.getMethod().getDescription();
        if (name == null || name.isEmpty()) name = result.getName();
        extentTest.set(extent.createTest(name));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().pass("Test PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        extentTest.get().fail(result.getThrowable());
        attachScreenshot("Screenshot on failure");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().skip(
                result.getThrowable() != null
                        ? result.getThrowable()
                        : new Exception("Test skipped"));
    }

    // ── Screenshot helper ─────────────────────────────────────────────────────

    private void attachScreenshot(String title) {
        WebDriver driver = DriverManager.getDriver();
        if (!(driver instanceof TakesScreenshot)) return;
        try {
            String base64 = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BASE64);
            extentTest.get().fail(title,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
        } catch (Exception e) {
            extentTest.get().warning("Screenshot capture failed: " + e.getMessage());
        }
    }
}

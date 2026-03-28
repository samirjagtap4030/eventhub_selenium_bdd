package com.eventhub.utils;

import org.openqa.selenium.WebDriver;

/**
 * Thread-safe WebDriver holder.
 * BaseTest sets/removes the driver; the ExtentReportListener reads it
 * during onTestFailure to capture a screenshot without a direct dependency
 * on BaseTest.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {}

    public static void setDriver(WebDriver driver) {
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    /** Must be called in @AfterMethod to prevent thread-local memory leaks. */
    public static void removeDriver() {
        DRIVER.remove();
    }
}

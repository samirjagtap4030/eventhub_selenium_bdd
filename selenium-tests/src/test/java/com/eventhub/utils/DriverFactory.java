package com.eventhub.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Creates a WebDriver instance for the requested browser.
 * The browser name is supplied via testng.xml {@code <parameter name="browser">}
 * and defaults to {@code chrome} when not specified.
 *
 * <p>Supported values: {@code chrome}, {@code firefox}, {@code edge}
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver createDriver(String browser) {
        switch (browser.trim().toLowerCase()) {

            case "firefox": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = new FirefoxOptions();
                opts.addArguments("--start-maximized");
                return new FirefoxDriver(opts);
            }

            case "edge": {
                WebDriverManager.edgedriver().setup();
                EdgeOptions opts = new EdgeOptions();
                opts.addArguments("--start-maximized");
                opts.addArguments("--disable-notifications");
                return new EdgeDriver(opts);
            }

            default: { // chrome
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--start-maximized");
                opts.addArguments("--disable-notifications");
                return new ChromeDriver(opts);
            }
        }
    }
}

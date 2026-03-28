package com.eventhub.pages;

import com.eventhub.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Parent for all Page Objects.
 *
 * <p>Centralises three responsibilities that were duplicated in every page:
 * <ol>
 *   <li>Holding the {@code WebDriver} reference</li>
 *   <li>Creating {@code WebDriverWait} instances driven by {@code config.properties}
 *       — fixes the EXPLICIT_WAIT constant that was defined in BaseTest but
 *       ignored by pages that hardcoded {@code Duration.ofSeconds(15)}</li>
 *   <li>Calling {@code PageFactory.initElements()} so subclasses never forget</li>
 * </ol>
 */
public abstract class BasePage {

    protected final WebDriver driver;

    /** Full-length wait — used for expected page elements. */
    protected final WebDriverWait wait;

    /** Short wait — used for optional/boolean existence checks to avoid
     *  burning the full timeout on legitimately absent elements. */
    protected final WebDriverWait shortWait;

    protected BasePage(WebDriver driver) {
        int timeout = ConfigReader.getInt("timeout.explicit");
        this.driver    = driver;
        this.wait      = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(3, timeout / 3)));
        PageFactory.initElements(driver, this);
    }
}

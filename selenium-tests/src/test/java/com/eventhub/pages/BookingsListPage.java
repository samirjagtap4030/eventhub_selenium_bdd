package com.eventhub.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class BookingsListPage extends BasePage {

    @FindBy(css = "[data-testid='booking-card']")
    private List<WebElement> bookingCards;

    @FindBy(xpath = "//*[contains(text(),'No bookings yet')]")
    private WebElement emptyStateHeading;

    @FindBy(xpath = "//button[contains(text(),'Clear all bookings')]")
    private WebElement clearAllBookingsButton;

    public BookingsListPage(WebDriver driver) {
        super(driver);
    }

    public BookingsListPage open(String baseUrl) {
        driver.get(baseUrl + "/bookings");
        return this;
    }

    /** Returns true when at least one booking card is visible. */
    public boolean hasBookingCards() {
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(bookingCards));
            return !bookingCards.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** Finds the booking card that contains the given booking reference text. */
    public WebElement getCardByRef(String bookingRef) {
        wait.until(ExpectedConditions.visibilityOfAllElements(bookingCards));
        return bookingCards.stream()
                .filter(card -> card.getText().contains(bookingRef))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Booking card with ref '" + bookingRef + "' not found"));
    }

    /** Returns the full text of the booking card matching the given ref. */
    public String getCardText(String bookingRef) {
        return getCardByRef(bookingRef).getText();
    }

    public boolean isEmptyStateVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOf(emptyStateHeading));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clicks "View Details" inside the card matching the given booking ref.
     * The link wraps a Button component, so it is matched by href.
     */
    public void clickViewDetails(String bookingRef) {
        WebElement card = getCardByRef(bookingRef);
        WebElement link = card.findElement(By.cssSelector("a[href*='/bookings/']"));
        wait.until(ExpectedConditions.elementToBeClickable(link));
        link.click();
    }

    /**
     * Clears all bookings. Accepts the browser {@code confirm()} dialog.
     * Safe to call when the list is already empty (uses shortWait to avoid 15 s penalty).
     */
    public void clearAllBookings() {
        boolean alreadyEmpty;
        try {
            shortWait.until(ExpectedConditions.visibilityOf(emptyStateHeading));
            alreadyEmpty = true;
        } catch (Exception e) {
            alreadyEmpty = false;
        }
        if (alreadyEmpty) return;

        wait.until(ExpectedConditions.elementToBeClickable(clearAllBookingsButton));
        clearAllBookingsButton.click();
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.visibilityOf(emptyStateHeading));
    }
}

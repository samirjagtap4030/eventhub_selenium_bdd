package com.eventhub.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BookingConfirmationPage extends BasePage {

    @FindBy(css = ".booking-ref")
    private WebElement bookingRefElement;

    @FindBy(css = "a[href*='/bookings']")
    private WebElement viewMyBookingsLink;

    public BookingConfirmationPage(WebDriver driver) {
        super(driver);
    }

    /** Returns the booking reference from the confirmation card (e.g. "T-AB12CD"). */
    public String getBookingRef() {
        wait.until(ExpectedConditions.visibilityOf(bookingRefElement));
        return bookingRefElement.getText().trim();
    }

    public void clickViewMyBookings() {
        wait.until(ExpectedConditions.elementToBeClickable(viewMyBookingsLink));
        viewMyBookingsLink.click();
    }
}

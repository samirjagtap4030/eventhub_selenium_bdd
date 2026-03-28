package com.eventhub.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BookingDetailPage extends BasePage {

    @FindBy(css = "span.font-mono.font-bold")
    private WebElement bookingRefBreadcrumb;

    @FindBy(xpath = "//*[contains(text(),'Event Details')]")
    private WebElement eventDetailsSection;

    @FindBy(xpath = "//*[contains(text(),'Customer Details')]")
    private WebElement customerDetailsSection;

    @FindBy(xpath = "//*[contains(text(),'Payment Summary')]")
    private WebElement paymentSummarySection;

    @FindBy(xpath = "//*[contains(text(),'Total Paid')]")
    private WebElement totalPaidLabel;

    @FindBy(id = "check-refund-btn")
    private WebElement checkRefundButton;

    @FindBy(xpath = "//button[contains(text(),'Cancel Booking')]")
    private WebElement cancelBookingButton;

    @FindBy(xpath = "//*[contains(text(),'Cancel this booking?')]")
    private WebElement cancelDialogHeading;

    @FindBy(id = "confirm-dialog-yes")
    private WebElement confirmYesButton;

    @FindBy(xpath = "//*[contains(text(),'Booking cancelled successfully')]")
    private WebElement cancellationToast;

    public BookingDetailPage(WebDriver driver) {
        super(driver);
    }

    public String getBookingRefFromBreadcrumb() {
        wait.until(ExpectedConditions.visibilityOf(bookingRefBreadcrumb));
        return bookingRefBreadcrumb.getText().trim();
    }

    public boolean isEventDetailsSectionVisible()    { return isVisible(eventDetailsSection); }
    public boolean isCustomerDetailsSectionVisible() { return isVisible(customerDetailsSection); }
    public boolean isPaymentSummarySectionVisible()  { return isVisible(paymentSummarySection); }
    public boolean isTotalPaidLabelVisible()          { return isVisible(totalPaidLabel); }
    public boolean isCheckRefundButtonVisible()       { return isVisible(checkRefundButton); }
    public boolean isCancellationToastVisible()       { return isVisible(cancellationToast); }

    /** Waits for the detail page URL pattern {@code /bookings/:id}. */
    public BookingDetailPage waitForPage() {
        wait.until(ExpectedConditions.urlMatches(".*/bookings/\\d+.*"));
        wait.until(ExpectedConditions.visibilityOf(cancelBookingButton));
        return this;
    }

    /**
     * Cancels the booking and waits for {@code router.push('/bookings')} to complete.
     * Returns the {@link BookingsListPage} the app navigates to, enabling page-chaining:
     * <pre>
     *   BookingsListPage list = detailPage.cancelBooking();
     *   Assert.assertTrue(list.isEmptyStateVisible());
     * </pre>
     */
    public BookingsListPage cancelBooking() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelBookingButton));
        cancelBookingButton.click();

        wait.until(ExpectedConditions.visibilityOf(cancelDialogHeading));
        wait.until(ExpectedConditions.elementToBeClickable(confirmYesButton));
        confirmYesButton.click();

        wait.until(ExpectedConditions.urlMatches(".*/bookings$"));
        return new BookingsListPage(driver);
    }

    private boolean isVisible(WebElement element) {
        try {
            shortWait.until(ExpectedConditions.visibilityOf(element));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

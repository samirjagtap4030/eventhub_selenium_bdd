package com.eventhub.bdd.steps;

import com.eventhub.bdd.context.TestContext;
import com.eventhub.pages.BookingConfirmationPage;
import com.eventhub.pages.BookingDetailPage;
import com.eventhub.pages.BookingFormPage;
import com.eventhub.pages.BookingsListPage;
import com.eventhub.pages.EventsPage;
import com.eventhub.pages.LoginPage;
import com.eventhub.utils.TestData;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Step definitions for {@code booking_management.feature}.
 *
 * <p>All steps use domain language — no UI widget names, no technical
 * terms.  Every step delegates to a page object; no WebDriver calls
 * appear here directly.
 *
 * <p>{@link TestContext} is injected by PicoContainer and shared with
 * {@link com.eventhub.bdd.hooks.CucumberHooks} within the same scenario.
 */
public class BookingManagementSteps {

    private static final Logger log = LoggerFactory.getLogger(BookingManagementSteps.class);

    private final TestContext ctx;

    public BookingManagementSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    private WebDriver driver() {
        return ctx.getDriver();
    }

    // ── Background ────────────────────────────────────────────────────────────

    @Given("the user is logged in with valid credentials")
    public void theUserIsLoggedInWithValidCredentials() {
        log.info("Step: logging in as {}", ctx.userEmail);
        new LoginPage(driver()).loginAs(ctx.baseUrl, ctx.userEmail, ctx.userPassword);
    }

    @And("all existing bookings are cleared")
    public void allExistingBookingsAreCleared() {
        log.info("Step: clearing all existing bookings");
        new BookingsListPage(driver()).open(ctx.baseUrl).clearAllBookings();
    }

    @And("the user books the first available event")
    public void theUserBooksTheFirstAvailableEvent() {
        log.info("Step: booking first available event");
        String eventTitle = new EventsPage(driver()).open(ctx.baseUrl).clickFirstAvailableBookNow();
        log.info("Booking event: \"{}\"", eventTitle);

        new BookingFormPage(driver())
                .waitForPage()
                .fillFullName(TestData.CUSTOMER_NAME)
                .fillEmail(TestData.CUSTOMER_EMAIL)
                .fillPhone(TestData.CUSTOMER_PHONE)
                .submitBooking();

        ctx.bookingRef = new BookingConfirmationPage(driver()).getBookingRef();
        log.info("Booking confirmed — ref: {}", ctx.bookingRef);
    }

    // ── Shared navigation steps ───────────────────────────────────────────────

    @When("the user views their bookings")
    public void theUserViewsTheirBookings() {
        log.info("Step: opening bookings list");
        new BookingsListPage(driver()).open(ctx.baseUrl);
    }

    @And("the user opens their booking")
    public void theUserOpensTheirBooking() {
        log.info("Step: opening booking detail for ref: {}", ctx.bookingRef);
        new BookingsListPage(driver()).clickViewDetails(ctx.bookingRef);
        new BookingDetailPage(driver()).waitForPage();
    }

    // ── Scenario 1: Booking appears in the bookings list ─────────────────────

    @Then("at least one booking should be listed")
    public void atLeastOneBookingShouldBeListed() {
        log.info("Step: asserting at least one booking is listed");
        Assert.assertTrue(
                new BookingsListPage(driver()).hasBookingCards(),
                "Expected at least one booking to be listed");
    }

    @And("the booking should show a confirmed status")
    public void theBookingShouldShowAConfirmedStatus() {
        log.info("Step: asserting booking status is confirmed — ref: {}", ctx.bookingRef);
        String cardText = new BookingsListPage(driver()).getCardText(ctx.bookingRef);
        Assert.assertTrue(
                cardText.toLowerCase().contains("confirmed"),
                "Booking [" + ctx.bookingRef + "] should show confirmed status but card text was: "
                        + cardText);
    }

    // ── Scenario 2: Event and customer information ────────────────────────────

    @Then("the booking reference should be confirmed")
    public void theBookingReferenceShouldBeConfirmed() {
        log.info("Step: asserting booking reference is shown — ref: {}", ctx.bookingRef);
        String displayed = new BookingDetailPage(driver()).getBookingRefFromBreadcrumb();
        Assert.assertTrue(
                displayed.contains(ctx.bookingRef),
                "Expected booking ref [" + ctx.bookingRef + "] to be shown but found: " + displayed);
    }

    @And("the booked event details should be shown")
    public void theBookedEventDetailsShouldBeShown() {
        log.info("Step: asserting event details are shown");
        Assert.assertTrue(
                new BookingDetailPage(driver()).isEventDetailsSectionVisible(),
                "Booked event details should be shown on the booking page");
    }

    @And("the customer information should be shown")
    public void theCustomerInformationShouldBeShown() {
        log.info("Step: asserting customer information is shown");
        Assert.assertTrue(
                new BookingDetailPage(driver()).isCustomerDetailsSectionVisible(),
                "Customer information should be shown on the booking page");
    }

    // ── Scenario 3: Payment information ──────────────────────────────────────

    @Then("the payment summary should be shown")
    public void thePaymentSummaryShouldBeShown() {
        log.info("Step: asserting payment summary is shown");
        Assert.assertTrue(
                new BookingDetailPage(driver()).isPaymentSummarySectionVisible(),
                "Payment summary should be shown on the booking page");
    }

    @And("the total amount paid should be displayed")
    public void theTotalAmountPaidShouldBeDisplayed() {
        log.info("Step: asserting total amount paid is displayed");
        Assert.assertTrue(
                new BookingDetailPage(driver()).isTotalPaidLabelVisible(),
                "Total amount paid should be displayed in the payment summary");
    }

    @And("the option to check refund eligibility should be available")
    public void theOptionToCheckRefundEligibilityShouldBeAvailable() {
        log.info("Step: asserting refund eligibility option is available");
        Assert.assertTrue(
                new BookingDetailPage(driver()).isCheckRefundButtonVisible(),
                "Option to check refund eligibility should be available on the booking page");
    }

    // ── Scenario 4: Cancel booking ────────────────────────────────────────────

    @And("the user cancels the booking")
    public void theUserCancelsTheBooking() {
        log.info("Step: cancelling booking ref: {}", ctx.bookingRef);
        new BookingDetailPage(driver()).cancelBooking();
    }

    @Then("the user should be returned to their bookings list")
    public void theUserShouldBeReturnedToTheirBookingsList() {
        log.info("Step: asserting user is back on bookings list");
        Assert.assertTrue(
                driver().getCurrentUrl().endsWith("/bookings"),
                "User should be returned to their bookings list after cancellation");
    }

    @And("a cancellation confirmation should be displayed")
    public void aCancellationConfirmationShouldBeDisplayed() {
        log.info("Step: asserting cancellation confirmation is shown");
        Assert.assertTrue(
                new BookingDetailPage(driver()).isCancellationToastVisible(),
                "A cancellation confirmation should be displayed after cancelling");
    }

    @And("no remaining bookings should be shown")
    public void noRemainingBookingsShouldBeShown() {
        log.info("Step: asserting no remaining bookings");
        Assert.assertTrue(
                new BookingsListPage(driver()).isEmptyStateVisible(),
                "No remaining bookings should be shown after cancellation");
    }
}

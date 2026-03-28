package com.eventhub.tests;

import com.eventhub.base.BaseTest;
import com.eventhub.pages.*;
import com.eventhub.utils.RetryAnalyzer;
import com.eventhub.utils.TestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * E2E — Booking Management: Critical Happy Paths (Tier 1 / P0)
 *
 * Groups
 * ──────
 * smoke      → TC-001  (fast PR gate — verifies booking list renders)
 * regression → TC-001 + TC-002 + TC-003  (full suite, run nightly)
 *
 * Priority
 * ────────
 * TC-001 (read)     priority=1
 * TC-002 (read)     priority=2
 * TC-003 (cancel)   priority=3  ← destructive; must run last
 */
public class BookingManagementTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BookingManagementTest.class);

    /** Booking reference captured in @BeforeMethod, consumed by each @Test. */
    private String bookingRef;

    // ── Shared setup ──────────────────────────────────────────────────────────

    /**
     * Runs after BaseTest.setUp() (parent @BeforeMethod executes first).
     * Guarantees each test starts with exactly one fresh, confirmed booking.
     */
    @BeforeMethod(alwaysRun = true)
    public void bookingSetup() {
        log.info("=== Setup: login → clear → book ===");

        new LoginPage(driver).loginAs(BASE_URL, USER_EMAIL, USER_PASSWORD);
        log.debug("Logged in as {}", USER_EMAIL);

        new BookingsListPage(driver).open(BASE_URL).clearAllBookings();
        log.debug("Existing bookings cleared");

        String eventTitle = new EventsPage(driver).open(BASE_URL).clickFirstAvailableBookNow();
        log.info("Booking event: \"{}\"", eventTitle);

        new BookingFormPage(driver)
                .waitForPage()
                .fillFullName(TestData.CUSTOMER_NAME)
                .fillEmail(TestData.CUSTOMER_EMAIL)
                .fillPhone(TestData.CUSTOMER_PHONE)
                .submitBooking();

        bookingRef = new BookingConfirmationPage(driver).getBookingRef();
        log.info("Booking confirmed — ref: {}", bookingRef);
    }

    // ── TC-001 ────────────────────────────────────────────────────────────────

    /**
     * TC-001 — View bookings list with existing bookings
     *
     * Groups: smoke + regression (fastest check; runs on every PR)
     */
    @Test(
        description   = "TC-001: Booking card appears on the bookings list page",
        groups        = {"smoke", "regression"},
        priority      = 1,
        retryAnalyzer = RetryAnalyzer.class
    )
    public void tc001_bookingCardAppearsOnListPage() {
        log.info("TC-001 — verifying booking card on /bookings");
        BookingsListPage bookingsPage = new BookingsListPage(driver).open(BASE_URL);

        Assert.assertTrue(
                bookingsPage.hasBookingCards(),
                "Expected at least one booking card on /bookings");

        String cardText = bookingsPage.getCardText(bookingRef);

        Assert.assertTrue(
                cardText.toLowerCase().contains("confirmed"),
                "Booking card should show status 'confirmed'");

        log.info("TC-001 PASSED — card found with ref: {}", bookingRef);
    }

    // ── TC-002 ────────────────────────────────────────────────────────────────

    /**
     * TC-002 — View single booking detail page
     *
     * Groups: regression
     * SoftAssert collects all section failures before throwing.
     */
    @Test(
        description   = "TC-002: All sections render correctly on the booking detail page",
        groups        = {"regression"},
        priority      = 2,
        retryAnalyzer = RetryAnalyzer.class
    )
    public void tc002_bookingDetailPageSectionsVisible() {
        log.info("TC-002 — verifying detail page sections for ref: {}", bookingRef);

        BookingsListPage bookingsPage = new BookingsListPage(driver).open(BASE_URL);
        bookingsPage.clickViewDetails(bookingRef);

        BookingDetailPage detailPage = new BookingDetailPage(driver).waitForPage();

        SoftAssert sa = new SoftAssert();

        sa.assertTrue(
                detailPage.getBookingRefFromBreadcrumb().contains(bookingRef),
                "Breadcrumb should contain booking ref: " + bookingRef);

        sa.assertTrue(detailPage.isEventDetailsSectionVisible(),
                "Event Details section should be visible");

        sa.assertTrue(detailPage.isCustomerDetailsSectionVisible(),
                "Customer Details section should be visible");

        sa.assertTrue(detailPage.isPaymentSummarySectionVisible(),
                "Payment Summary section should be visible");

        sa.assertTrue(detailPage.isTotalPaidLabelVisible(),
                "'Total Paid' label should be visible inside Payment Summary");

        sa.assertTrue(detailPage.isCheckRefundButtonVisible(),
                "'Check Refund Eligibility' button should be visible");

        sa.assertAll();

        log.info("TC-002 PASSED — all sections visible for ref: {}", bookingRef);
    }

    // ── TC-003 ────────────────────────────────────────────────────────────────

    /**
     * TC-003 — Cancel a single booking from the detail page
     *
     * Groups: regression
     * priority=3 ensures this destructive test always runs last.
     */
    @Test(
        description   = "TC-003: Cancel booking from detail page shows toast and redirects",
        groups        = {"regression"},
        priority      = 3,
        retryAnalyzer = RetryAnalyzer.class
    )
    public void tc003_cancelBookingFromDetailPage() {
        log.info("TC-003 — cancelling booking ref: {}", bookingRef);

        new BookingsListPage(driver).open(BASE_URL).clickViewDetails(bookingRef);
        BookingDetailPage detailPage = new BookingDetailPage(driver).waitForPage();

        BookingsListPage listAfterCancel = detailPage.cancelBooking();

        Assert.assertTrue(
                driver.getCurrentUrl().endsWith("/bookings"),
                "Should redirect to /bookings after cancellation");

        Assert.assertTrue(
                detailPage.isCancellationToastVisible(),
                "Success toast 'Booking cancelled successfully' should appear");

        Assert.assertTrue(
                listAfterCancel.isEmptyStateVisible(),
                "'No bookings yet' empty state should be visible after cancellation");

        log.info("TC-003 PASSED — booking {} cancelled, empty state confirmed", bookingRef);
    }
}

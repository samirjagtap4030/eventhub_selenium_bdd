package com.eventhub.utils;

/**
 * Central repository for test input data.
 * Keeps magic strings out of test and setup methods.
 */
public final class TestData {

    // ── Customer details used in the booking form ─────────────────────────────
    public static final String CUSTOMER_NAME  = "Test User";
    public static final String CUSTOMER_EMAIL = "testuser@example.com";
    public static final String CUSTOMER_PHONE = "9876543210";

    private TestData() {}
}

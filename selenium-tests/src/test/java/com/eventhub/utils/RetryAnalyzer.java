package com.eventhub.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test once before marking it as failed.
 * Applied via {@code @Test(retryAnalyzer = RetryAnalyzer.class)}.
 *
 * One retry guards against transient network issues on the live site
 * without masking real failures (more than 1 retry would do that).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRIES = 1;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE && retryCount < MAX_RETRIES) {
            retryCount++;
            log.warn("[Retry] Retrying \"{}\" — attempt {} of {}", result.getName(), retryCount, MAX_RETRIES);
            return true;
        }
        return false;
    }
}

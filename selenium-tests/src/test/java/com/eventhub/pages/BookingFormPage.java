package com.eventhub.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BookingFormPage extends BasePage {

    @FindBy(id = "customerName")
    private WebElement fullNameInput;

    @FindBy(id = "customer-email")
    private WebElement emailInput;

    @FindBy(css = "input[placeholder='+91 98765 43210']")
    private WebElement phoneInput;

    @FindBy(css = ".confirm-booking-btn")
    private WebElement confirmButton;

    public BookingFormPage(WebDriver driver) {
        super(driver);
    }

    /** Waits for the event detail / booking form URL before interacting. */
    public BookingFormPage waitForPage() {
        wait.until(ExpectedConditions.urlMatches(".*/events/\\d+.*"));
        wait.until(ExpectedConditions.visibilityOf(phoneInput));
        return this;
    }

    public BookingFormPage fillFullName(String name) {
        wait.until(ExpectedConditions.visibilityOf(fullNameInput));
        fullNameInput.clear();
        fullNameInput.sendKeys(name);
        return this;
    }

    public BookingFormPage fillEmail(String email) {
        emailInput.clear();
        emailInput.sendKeys(email);
        return this;
    }

    public BookingFormPage fillPhone(String phone) {
        phoneInput.clear();
        phoneInput.sendKeys(phone);
        return this;
    }

    public void submitBooking() {
        wait.until(ExpectedConditions.elementToBeClickable(confirmButton));
        confirmButton.click();
    }
}

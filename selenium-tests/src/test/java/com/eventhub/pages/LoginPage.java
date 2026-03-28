package com.eventhub.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    @FindBy(css = "input[placeholder='you@email.com']")
    private WebElement emailInput;

    @FindBy(css = "input[type='password']")
    private WebElement passwordInput;

    @FindBy(id = "login-btn")
    private WebElement loginButton;

    @FindBy(css = "a[href*='/events']")
    private WebElement browseEventsLink;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        return this;
    }

    public LoginPage enterEmail(String email) {
        wait.until(ExpectedConditions.visibilityOf(emailInput));
        emailInput.clear();
        emailInput.sendKeys(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        passwordInput.clear();
        passwordInput.sendKeys(password);
        return this;
    }

    public void clickLogin() {
        loginButton.click();
    }

    /** Full login sequence — navigates, fills form, waits for home page. */
    public void loginAs(String baseUrl, String email, String password) {
        open(baseUrl);
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        wait.until(ExpectedConditions.visibilityOf(browseEventsLink));
    }
}

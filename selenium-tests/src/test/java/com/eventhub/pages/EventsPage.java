package com.eventhub.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class EventsPage extends BasePage {

    @FindBy(css = "[data-testid='event-card']")
    private List<WebElement> eventCards;

    public EventsPage(WebDriver driver) {
        super(driver);
    }

    public EventsPage open(String baseUrl) {
        driver.get(baseUrl + "/events");
        return this;
    }

    /**
     * Finds the first event card with a visible "Book Now" button and clicks it.
     * Uses Selenium 4 {@code Actions.scrollToElement()} to centre the button
     * in the viewport before clicking, preventing the sandbox banner (sticky
     * footer) from intercepting the click.
     *
     * @return the title of the event that was booked
     */
    public String clickFirstAvailableBookNow() {
        wait.until(ExpectedConditions.visibilityOfAllElements(eventCards));
        Actions actions = new Actions(driver);

        for (WebElement card : eventCards) {
            List<WebElement> bookButtons = card.findElements(
                    By.cssSelector("[data-testid='book-now-btn']"));

            if (!bookButtons.isEmpty() && bookButtons.get(0).isDisplayed()) {
                String title = card.findElement(By.tagName("h3")).getText().trim();
                WebElement btn = bookButtons.get(0);
                actions.scrollToElement(btn).click(btn).perform();
                return title;
            }
        }
        throw new RuntimeException("No available events found on the events page");
    }
}

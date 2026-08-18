package org.bharathi.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {

    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
    private static final By SIGN_UP_LINK = By.cssSelector("a[href='/register']");

    private final WebDriver driver;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public LoginPage enterEmail(String email) {
        driver.findElement(EMAIL_INPUT).sendKeys(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        return this;
    }

    public void submit() {
        driver.findElement(SUBMIT_BUTTON).click();
    }

    public boolean isDisplayed() {
        return driver.findElements(EMAIL_INPUT).stream().anyMatch(WebElement::isDisplayed);
    }

    public void goToSignUp() {
        driver.findElement(SIGN_UP_LINK).click();
    }
}

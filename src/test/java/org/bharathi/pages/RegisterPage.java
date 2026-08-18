package org.bharathi.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class RegisterPage {

    private static final By HEADING = By.cssSelector("h1");
    private static final By NAME_INPUT = By.id("name");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By CREATE_ACCOUNT_BUTTON = By.xpath("//button[normalize-space()='Create account']");
    private static final By LOG_IN_LINK = By.cssSelector("a[href='/login']");

    private final WebDriver driver;

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return driver.findElements(HEADING).stream().anyMatch(WebElement::isDisplayed);
    }

    public RegisterPage enterFullName(String fullName) {
        driver.findElement(NAME_INPUT).sendKeys(fullName);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        driver.findElement(EMAIL_INPUT).sendKeys(email);
        return this;
    }

    public RegisterPage enterPhone(String phone) {
        driver.findElement(PHONE_INPUT).sendKeys(phone);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        return this;
    }

    public void createAccount() {
        driver.findElement(CREATE_ACCOUNT_BUTTON).click();
    }

    public void goToLogIn() {
        driver.findElement(LOG_IN_LINK).click();
    }
}

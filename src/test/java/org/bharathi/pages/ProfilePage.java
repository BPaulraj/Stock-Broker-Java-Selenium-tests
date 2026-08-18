package org.bharathi.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;

public class ProfilePage {

    private static final By HEADING = By.cssSelector("main h1");

    // Account summary (read-only)
    private static final By EMAIL_VALUE = By.xpath("//dt[normalize-space()='Email']/following-sibling::dd");
    private static final By MEMBER_SINCE_VALUE = By.xpath("//dt[normalize-space()='Member since']/following-sibling::dd");
    private static final By KYC_STATUS_BADGE = By.xpath("//dt[normalize-space()='KYC status']/following-sibling::dd//span[1]");

    // Edit details form
    private static final By NAME_INPUT = By.id("name");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By ADDRESS_INPUT = By.id("address");
    private static final By SAVE_BUTTON = By.xpath("//button[normalize-space()='Save changes']");
    private static final By STATUS_MESSAGE = By.cssSelector("main form div.bg-emerald-50, main form div.bg-red-50");

    private final WebDriver driver;

    public ProfilePage(WebDriver driver) {
        this.driver = driver;
    }

    public String getHeading() {
        return driver.findElement(HEADING).getText();
    }

    // Account summary

    public String getEmail() {
        return driver.findElement(EMAIL_VALUE).getText();
    }

    public String getMemberSince() {
        return driver.findElement(MEMBER_SINCE_VALUE).getText();
    }

    public String getKycStatus() {
        return driver.findElement(KYC_STATUS_BADGE).getText();
    }

    // Edit details

    public String getFullName() {
        return driver.findElement(NAME_INPUT).getAttribute("value");
    }

    public String getPhone() {
        return driver.findElement(PHONE_INPUT).getAttribute("value");
    }

    public String getAddress() {
        return driver.findElement(ADDRESS_INPUT).getAttribute("value");
    }

    public void setFullName(String fullName) {
        replaceValue(NAME_INPUT, fullName);
    }

    public void setPhone(String phone) {
        replaceValue(PHONE_INPUT, phone);
    }

    public void setAddress(String address) {
        replaceValue(ADDRESS_INPUT, address);
    }

    private void replaceValue(By locator, String value) {
        var field = driver.findElement(locator);
        field.clear();
        field.sendKeys(value);
    }

    public void saveChanges() {
        driver.findElement(SAVE_BUTTON).click();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(STATUS_MESSAGE));
    }

    public Optional<String> getStatusMessage() {
        return driver.findElements(STATUS_MESSAGE).stream()
                .findFirst()
                .map(org.openqa.selenium.WebElement::getText);
    }
}

package org.bharathi.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class InboxPage {

    private static final By HEADING = By.cssSelector("main h1");
    private static final By MESSAGE_ROWS = By.cssSelector("main ul.divide-y > li > button");

    // Detail panel (right column) — badge/timestamp located relative to the one <h2> on the page
    private static final By DETAIL_TYPE = By.xpath("//h2/preceding-sibling::span[1]");
    private static final By DETAIL_SUBJECT = By.cssSelector("main h2");
    private static final By DETAIL_TIMESTAMP = By.xpath("//h2/following-sibling::p[1]");
    private static final By DETAIL_BODY = By.cssSelector("main p.whitespace-pre-wrap");
    private static final By DETAIL_INVOICE_LINK = By.cssSelector("main a[href*='/api/invoices/']");
    private static final By MARK_READ_BUTTON = By.xpath("//button[normalize-space()='Mark as unread' or normalize-space()='Mark as read']");

    private final WebDriver driver;

    public InboxPage(WebDriver driver) {
        this.driver = driver;
    }

    private void waitFor(By locator) {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public String getHeading() {
        return driver.findElement(HEADING).getText();
    }

    public record MessageSummary(String subject, String type, String timestamp, boolean unread) {
    }

    public List<MessageSummary> getMessages() {
        waitFor(MESSAGE_ROWS);
        return driver.findElements(MESSAGE_ROWS).stream()
                .map(row -> new MessageSummary(
                        row.findElement(By.cssSelector("span.truncate")).getText(),
                        row.findElements(By.cssSelector("span.rounded-full")).stream()
                                .map(org.openqa.selenium.WebElement::getText)
                                .filter(text -> !text.isEmpty())
                                .findFirst()
                                .orElse(""),
                        row.findElement(By.cssSelector("span.text-slate-400")).getText(),
                        !row.findElements(By.cssSelector("span.bg-brand-600")).isEmpty()))
                .toList();
    }

    public void selectMessage(String subjectContains) {
        By locator = By.xpath("//main//ul/li/button[contains(., '" + subjectContains + "')]");
        waitFor(locator);
        driver.findElement(locator).click();
    }

    // Detail panel

    public String getDetailType() {
        return driver.findElement(DETAIL_TYPE).getText();
    }

    public String getDetailSubject() {
        return driver.findElement(DETAIL_SUBJECT).getText();
    }

    public String getDetailTimestamp() {
        return driver.findElement(DETAIL_TIMESTAMP).getText();
    }

    public String getDetailBody() {
        return driver.findElement(DETAIL_BODY).getText();
    }

    public Optional<String> getInvoiceLinkHref() {
        return driver.findElements(DETAIL_INVOICE_LINK).stream()
                .findFirst()
                .map(link -> link.getAttribute("href"));
    }

    public String getMarkReadButtonText() {
        return driver.findElement(MARK_READ_BUTTON).getText();
    }

    public void toggleReadState() {
        driver.findElement(MARK_READ_BUTTON).click();
    }
}

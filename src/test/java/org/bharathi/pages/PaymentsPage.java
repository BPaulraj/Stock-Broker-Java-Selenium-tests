package org.bharathi.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class PaymentsPage {

    private static final By HEADING = By.cssSelector("main h1");
    private static final By WALLET_BALANCE_VALUE = By.xpath("//p[normalize-space()='Wallet balance']/following-sibling::p");

    private static final By BANK_TRANSFER_TAB = By.xpath("//button[normalize-space()='Bank transfer']");
    private static final By DEBIT_CARD_TAB = By.xpath("//button[normalize-space()='Debit card']");

    private static final By AMOUNT_INPUT = By.id("amount");

    // Bank transfer fields
    private static final By ACCOUNT_NUMBER_INPUT = By.id("accountNumber");
    private static final By IFSC_INPUT = By.id("ifsc");

    // Debit card fields
    private static final By CARD_NUMBER_INPUT = By.id("cardNumber");
    private static final By EXPIRY_INPUT = By.id("expiry");
    private static final By CVV_INPUT = By.id("cvv");

    private static final By ADD_FUNDS_BUTTON = By.xpath("//button[normalize-space()='Add funds']");

    private static final By TRANSACTION_ROWS = By.xpath("//h2[normalize-space()='Transaction history']/following-sibling::div//table/tbody/tr");

    // Shown after submit; success uses bg-emerald-50, failure bg-red-50. Submission is
    // async (the button reads "Processing bank transfer…"/"Processing card payment…" and
    // is disabled in between), so addFunds() waits for this before returning.
    private static final By STATUS_MESSAGE = By.cssSelector("main form div.bg-emerald-50, main form div.bg-red-50");

    private final WebDriver driver;

    public PaymentsPage(WebDriver driver) {
        this.driver = driver;
    }

    private org.openqa.selenium.WebElement waitFor(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public String getHeading() {
        return driver.findElement(HEADING).getText();
    }

    public String getWalletBalance() {
        return driver.findElement(WALLET_BALANCE_VALUE).getText();
    }

    public void selectBankTransferTab() {
        driver.findElement(BANK_TRANSFER_TAB).click();
    }

    public void selectDebitCardTab() {
        driver.findElement(DEBIT_CARD_TAB).click();
    }

    public void enterAmount(String amount) {
        driver.findElement(AMOUNT_INPUT).sendKeys(amount);
    }

    // Bank transfer

    public void enterAccountNumber(String accountNumber) {
        driver.findElement(ACCOUNT_NUMBER_INPUT).sendKeys(accountNumber);
    }

    public void enterIfsc(String ifsc) {
        driver.findElement(IFSC_INPUT).sendKeys(ifsc);
    }

    // Debit card

    public void enterCardNumber(String cardNumber) {
        driver.findElement(CARD_NUMBER_INPUT).sendKeys(cardNumber);
    }

    public void enterExpiry(String expiry) {
        driver.findElement(EXPIRY_INPUT).sendKeys(expiry);
    }

    public void enterCvv(String cvv) {
        driver.findElement(CVV_INPUT).sendKeys(cvv);
    }

    public boolean areDebitCardFieldsDisplayed() {
        return driver.findElement(CARD_NUMBER_INPUT).isDisplayed()
                && driver.findElement(EXPIRY_INPUT).isDisplayed()
                && driver.findElement(CVV_INPUT).isDisplayed();
    }

    public void addFunds() {
        driver.findElement(ADD_FUNDS_BUTTON).click();
        waitFor(STATUS_MESSAGE);
    }

    public String getStatusMessage() {
        return waitFor(STATUS_MESSAGE).getText();
    }

    public record Transaction(String date, String description, String method, String status, String amount) {
    }

    public List<Transaction> getTransactionHistory() {
        waitFor(TRANSACTION_ROWS);
        return driver.findElements(TRANSACTION_ROWS).stream()
                .map(row -> row.findElements(By.tagName("td")))
                .map(cells -> new Transaction(
                        cells.get(0).getText(),
                        cells.get(1).getText(),
                        cells.get(2).getText(),
                        cells.get(3).getText(),
                        cells.get(4).getText()))
                .toList();
    }
}

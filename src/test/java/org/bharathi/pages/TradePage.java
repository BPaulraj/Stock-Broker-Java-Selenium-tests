package org.bharathi.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TradePage {

    private static final By HEADING = By.cssSelector("main h1");
    private static final By SEARCH_INPUT = By.id("search");
    private static final By COMPANY_ROWS = By.cssSelector("main table tbody tr");

    // Order-building panel (right column), shown after clicking Buy/Sell on a company row.
    // normalize-space(), not text()=, because "Buy"/"Sell order" is rendered as separate
    // text nodes (interpolated verb + static " order"), which text()= equality won't match.
    private static final By ORDER_PANEL_TITLE = By.xpath("//p[normalize-space()='Buy order' or normalize-space()='Sell order']");
    private static final By ORDER_PANEL_COMPANY = By.xpath("(//p[normalize-space()='Buy order' or normalize-space()='Sell order'])/following-sibling::p[1]");
    private static final By ORDER_PANEL_CURRENT_PRICE = By.xpath("(//p[normalize-space()='Buy order' or normalize-space()='Sell order'])/following-sibling::p[2]");
    private static final By QUANTITY_INPUT = By.id("quantity");
    private static final By ESTIMATED_TOTAL = By.xpath("//span[normalize-space()='Estimated total']/following-sibling::span");
    private static final By WALLET_BALANCE = By.xpath("//span[normalize-space()='Wallet balance']/following-sibling::span");
    private static final By REVIEW_ORDER_BUTTON = By.xpath("//button[normalize-space()='Review order']");
    private static final By CANCEL_BUTTON = By.xpath("//button[normalize-space()='Cancel']");

    // Confirm-order panel, shown after clicking "Review order"
    private static final By CONFIRM_SUMMARY = By.xpath("//p[normalize-space()='Confirm order']/following-sibling::p");
    private static final By CONFIRM_QUANTITY = By.xpath("//span[normalize-space()='Quantity']/following-sibling::span");
    private static final By CONFIRM_QUOTED_PRICE = By.xpath("//span[normalize-space()='Quoted price / share']/following-sibling::span");
    private static final By CONFIRM_ESTIMATED_TOTAL = By.xpath("//span[normalize-space()='Estimated total']/following-sibling::span");
    private static final By CONFIRM_BUTTON = By.xpath("//button[starts-with(normalize-space(),'Confirm ')]");
    private static final By BACK_BUTTON = By.xpath("//button[normalize-space()='Back']");

    // Page-level status banner (e.g. "Bought 1 share of AAPL at $230.43 — total $230.43."),
    // shown above the search box after a trade is confirmed.
    private static final By STATUS_MESSAGE = By.cssSelector("main div.bg-emerald-50");

    private final WebDriver driver;

    public TradePage(WebDriver driver) {
        this.driver = driver;
    }

    // The order/confirm panel re-renders on each simulated price tick, which can
    // transiently remove and recreate its elements; wait rather than find directly.
    private WebElement waitFor(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public String getHeading() {
        return driver.findElement(HEADING).getText();
    }

    public void searchCompanies(String query) {
        driver.findElement(SEARCH_INPUT).sendKeys(query);
    }

    public record Company(String ticker, String name, String sector, String price) {
    }

    public List<Company> getCompanies() {
        waitFor(COMPANY_ROWS);
        return driver.findElements(COMPANY_ROWS).stream()
                .map(row -> row.findElements(By.tagName("td")))
                .map(cells -> new Company(
                        cells.get(0).getText(),
                        cells.get(1).getText(),
                        cells.get(2).getText(),
                        cells.get(3).getText()))
                .toList();
    }

    private By buyButtonFor(String ticker) {
        return By.xpath("//tr[td[1][normalize-space()='" + ticker + "']]//button[normalize-space()='Buy']");
    }

    private By sellButtonFor(String ticker) {
        return By.xpath("//tr[td[1][normalize-space()='" + ticker + "']]//button[normalize-space()='Sell']");
    }

    public void clickBuy(String ticker) {
        waitFor(buyButtonFor(ticker)).click();
    }

    public void clickSell(String ticker) {
        waitFor(sellButtonFor(ticker)).click();
    }

    // Order-building panel

    public String getOrderPanelTitle() {
        return waitFor(ORDER_PANEL_TITLE).getText();
    }

    public String getOrderPanelCompany() {
        return waitFor(ORDER_PANEL_COMPANY).getText();
    }

    public String getOrderPanelCurrentPrice() {
        return waitFor(ORDER_PANEL_CURRENT_PRICE).getText();
    }

    public void enterQuantity(String quantity) {
        waitFor(QUANTITY_INPUT).sendKeys(quantity);
    }

    public String getEstimatedTotal() {
        return waitFor(ESTIMATED_TOTAL).getText();
    }

    public String getWalletBalance() {
        return waitFor(WALLET_BALANCE).getText();
    }

    public void reviewOrder() {
        waitFor(REVIEW_ORDER_BUTTON).click();
    }

    public void cancelOrder() {
        waitFor(CANCEL_BUTTON).click();
    }

    // Confirm-order panel

    public String getConfirmSummary() {
        return waitFor(CONFIRM_SUMMARY).getText();
    }

    public String getConfirmQuantity() {
        return waitFor(CONFIRM_QUANTITY).getText();
    }

    public String getConfirmQuotedPrice() {
        return waitFor(CONFIRM_QUOTED_PRICE).getText();
    }

    public String getConfirmEstimatedTotal() {
        return waitFor(CONFIRM_ESTIMATED_TOTAL).getText();
    }

    public void confirmOrder() {
        waitFor(CONFIRM_BUTTON).click();
        waitFor(STATUS_MESSAGE);
    }

    public void back() {
        waitFor(BACK_BUTTON).click();
    }

    public String getStatusMessage() {
        return waitFor(STATUS_MESSAGE).getText();
    }
}

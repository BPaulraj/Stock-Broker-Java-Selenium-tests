package org.bharathi.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DashboardPage {

    // Header
    private static final By LOGOUT_BUTTON = By.xpath("//header//button[normalize-space()='Log out']");
    private static final By USER_EMAIL = By.cssSelector("header span.sm\\:inline");

    // Top nav (the only <a> elements under <header>; scoping by header disambiguates
    // them from the identically-hrefed quick-link tiles under <main>)
    private static final By NAV_DASHBOARD = By.cssSelector("header a[href='/dashboard']");
    private static final By NAV_TRADE = By.cssSelector("header a[href='/trade']");
    private static final By NAV_PAYMENTS = By.cssSelector("header a[href='/payments']");
    private static final By NAV_INBOX = By.cssSelector("header a[href='/inbox']");
    private static final By NAV_PROFILE = By.cssSelector("header a[href='/profile']");
    private static final By NAV_INBOX_UNREAD_BADGE = By.cssSelector("header a[href='/inbox'] span");

    // Main content
    private static final By WELCOME_HEADING = By.cssSelector("main h1");
    private static final By WALLET_BALANCE_VALUE = By.xpath("//p[text()='Wallet balance']/following-sibling::p");
    private static final By PORTFOLIO_VALUE = By.xpath("//p[text()='Portfolio value']/following-sibling::p");
    private static final By TOTAL_GAIN_LOSS_VALUE = By.xpath("//p[text()='Total gain / loss']/following-sibling::p");

    // Holdings table
    private static final By HOLDINGS_ROWS = By.xpath("//h2[text()='Holdings']/following-sibling::div//table/tbody/tr");

    // Quick-link tiles (the identically-hrefed <a> elements under <main>, as opposed to <header>)
    private static final By QUICK_LINK_TRADE = By.cssSelector("main a[href='/trade']");
    private static final By QUICK_LINK_PAYMENTS = By.cssSelector("main a[href='/payments']");
    private static final By QUICK_LINK_PROFILE = By.cssSelector("main a[href='/profile']");
    private static final By QUICK_LINK_INBOX = By.cssSelector("main a[href='/inbox']");

    private final WebDriver driver;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_BUTTON));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // Header

    public void logout() {
        driver.findElement(LOGOUT_BUTTON).click();
    }

    public String getUserEmail() {
        return driver.findElement(USER_EMAIL).getText();
    }

    // Nav

    public void goToDashboard() {
        driver.findElement(NAV_DASHBOARD).click();
    }

    public void goToTrade() {
        driver.findElement(NAV_TRADE).click();
    }

    public void goToPayments() {
        driver.findElement(NAV_PAYMENTS).click();
    }

    public void goToInbox() {
        driver.findElement(NAV_INBOX).click();
    }

    public void goToProfile() {
        driver.findElement(NAV_PROFILE).click();
    }

    public String getInboxUnreadCount() {
        return driver.findElement(NAV_INBOX_UNREAD_BADGE).getText();
    }

    // Welcome + summary cards

    public String getWelcomeHeading() {
        return driver.findElement(WELCOME_HEADING).getText();
    }

    public String getWalletBalance() {
        return driver.findElement(WALLET_BALANCE_VALUE).getText();
    }

    public String getPortfolioValue() {
        return driver.findElement(PORTFOLIO_VALUE).getText();
    }

    public String getTotalGainLoss() {
        return driver.findElement(TOTAL_GAIN_LOSS_VALUE).getText();
    }

    // Holdings table

    public record Holding(String ticker, String qty, String avgCost, String price, String value, String gainLoss) {
    }

    public List<Holding> getHoldings() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(HOLDINGS_ROWS));
        return driver.findElements(HOLDINGS_ROWS).stream()
                .map(row -> row.findElements(By.tagName("td")))
                .map(cells -> new Holding(
                        cells.get(0).getText(),
                        cells.get(1).getText(),
                        cells.get(2).getText(),
                        cells.get(3).getText(),
                        cells.get(4).getText(),
                        cells.get(5).getText()))
                .toList();
    }

    public Holding getHolding(String ticker) {
        return getHoldings().stream()
                .filter(holding -> holding.ticker().equals(ticker))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("No holding found for ticker " + ticker));
    }

    // Quick-link tiles

    public void openTradeTile() {
        driver.findElement(QUICK_LINK_TRADE).click();
    }

    public void openPaymentsTile() {
        driver.findElement(QUICK_LINK_PAYMENTS).click();
    }

    public void openProfileTile() {
        driver.findElement(QUICK_LINK_PROFILE).click();
    }

    public void openInboxTile() {
        driver.findElement(QUICK_LINK_INBOX).click();
    }
}

package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.driver.DriverManager;
import org.bharathi.pages.DashboardPage;
import org.bharathi.pages.TradePage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TradeSteps {

    private double walletBalanceBefore;
    private String notedTicker;

    private TradePage trade() {
        return new TradePage(DriverManager.getDriver());
    }

    private static double toDollars(String text) {
        return Double.parseDouble(text.replace("$", "").replace(",", ""));
    }

    @Given("I go to the trade page")
    public void iGoToTheTradePage() {
        new DashboardPage(DriverManager.getDriver()).goToTrade();
    }

    @Then("the Trade page should be displayed")
    public void theTradePageShouldBeDisplayed() {
        assertEquals(trade().getHeading(), "Trade");
    }

    @When("I search for company {string}")
    public void iSearchForCompany(String query) {
        trade().searchCompanies(query);
    }

    @Then("every listed company should match {string}")
    public void everyListedCompanyShouldMatch(String query) {
        var companies = trade().getCompanies();
        assertFalse(companies.isEmpty(), "Expected the search to return at least one company");
        for (TradePage.Company company : companies) {
            String haystack = (company.ticker() + " " + company.name() + " " + company.sector()).toLowerCase();
            assertTrue(haystack.contains(query.toLowerCase()),
                    "Expected company " + company + " to match search term '" + query + "'");
        }
    }

    @Given("I note the wallet balance shown in the order panel for {string}")
    public void iNoteTheWalletBalanceShownInTheOrderPanelFor(String ticker) {
        notedTicker = ticker;
        trade().clickBuy(ticker);
        walletBalanceBefore = toDollars(trade().getWalletBalance());
        trade().cancelOrder();
    }

    @When("I buy 1 share of {string}")
    public void iBuyOneShareOf(String ticker) {
        trade().clickBuy(ticker);
        trade().enterQuantity("1");
        trade().reviewOrder();
        trade().confirmOrder();
    }

    @Then("the trade confirmation should mention {string}")
    public void theTradeConfirmationShouldMention(String ticker) {
        assertTrue(trade().getStatusMessage().contains(ticker),
                "Expected the trade confirmation to mention " + ticker + " but was: " + trade().getStatusMessage());
    }

    @Then("the trade wallet balance should have decreased")
    public void theTradeWalletBalanceShouldHaveDecreased() {
        trade().clickBuy(notedTicker);
        double walletBalanceAfter = toDollars(trade().getWalletBalance());
        trade().cancelOrder();
        assertTrue(walletBalanceAfter < walletBalanceBefore,
                "Expected wallet balance to decrease from $" + walletBalanceBefore + " but was $" + walletBalanceAfter);
    }
}

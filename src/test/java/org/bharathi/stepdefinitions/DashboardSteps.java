package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.driver.DriverManager;
import org.bharathi.pages.DashboardPage;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DashboardSteps {

    private DashboardPage dashboard() {
        return new DashboardPage(DriverManager.getDriver());
    }

    @Then("the welcome heading should greet the logged-in user")
    public void theWelcomeHeadingShouldGreetTheLoggedInUser() {
        assertTrue(dashboard().getWelcomeHeading().startsWith("Welcome,"),
                "Expected the welcome heading to start with 'Welcome,' but was: " + dashboard().getWelcomeHeading());
    }

    @Then("the wallet balance should be a dollar amount")
    public void theWalletBalanceShouldBeADollarAmount() {
        String balance = dashboard().getWalletBalance();
        assertTrue(balance.matches("\\$[0-9,]+\\.\\d{2}"),
                "Expected the wallet balance to look like a dollar amount but was: " + balance);
    }

    @Then("the holdings table should list at least one holding")
    public void theHoldingsTableShouldListAtLeastOneHolding() {
        assertFalse(dashboard().getHoldings().isEmpty(), "Expected the holdings table to have at least one row");
    }

    @When("I go to Trade from the nav")
    public void iGoToTradeFromTheNav() {
        dashboard().goToTrade();
    }

    @When("I go to Payments from the nav")
    public void iGoToPaymentsFromTheNav() {
        dashboard().goToPayments();
    }

    @When("I go to Inbox from the nav")
    public void iGoToInboxFromTheNav() {
        dashboard().goToInbox();
    }

    @When("I go to Profile from the nav")
    public void iGoToProfileFromTheNav() {
        dashboard().goToProfile();
    }

    @When("I open the {string} quick-link tile")
    public void iOpenTheQuickLinkTile(String tile) {
        switch (tile) {
            case "Trade" -> dashboard().openTradeTile();
            case "Payments" -> dashboard().openPaymentsTile();
            case "Profile" -> dashboard().openProfileTile();
            case "Inbox" -> dashboard().openInboxTile();
            default -> throw new IllegalArgumentException("Unknown quick-link tile: " + tile);
        }
    }
}

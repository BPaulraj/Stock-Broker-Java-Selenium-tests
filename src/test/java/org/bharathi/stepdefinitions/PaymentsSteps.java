package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.driver.DriverManager;
import org.bharathi.pages.DashboardPage;
import org.bharathi.pages.PaymentsPage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PaymentsSteps {

    private double walletBalanceBefore;

    private PaymentsPage payments() {
        return new PaymentsPage(DriverManager.getDriver());
    }

    private static double toDollars(String text) {
        return Double.parseDouble(text.replace("$", "").replace(",", ""));
    }

    @Given("I go to the payments page")
    public void iGoToThePaymentsPage() {
        new DashboardPage(DriverManager.getDriver()).goToPayments();
    }

    @Then("the Payments page should be displayed")
    public void thePaymentsPageShouldBeDisplayed() {
        assertEquals(payments().getHeading(), "Payments");
    }

    @Given("I note the wallet balance")
    public void iNoteTheWalletBalance() {
        walletBalanceBefore = toDollars(payments().getWalletBalance());
    }

    @When("I add funds of {string} via bank transfer")
    public void iAddFundsOfViaBankTransfer(String amount) {
        payments().enterAmount(amount);
        payments().enterAccountNumber("000123456789");
        payments().enterIfsc("DEMO0123456");
        payments().addFunds();
    }

    @Then("the payment confirmation should mention the new balance")
    public void thePaymentConfirmationShouldMentionTheNewBalance() {
        assertTrue(payments().getStatusMessage().contains("New balance"),
                "Expected the payment confirmation to mention the new balance but was: " + payments().getStatusMessage());
    }

    @Then("the wallet balance should have increased")
    public void theWalletBalanceShouldHaveIncreased() {
        double walletBalanceAfter = toDollars(payments().getWalletBalance());
        assertTrue(walletBalanceAfter > walletBalanceBefore,
                "Expected wallet balance to increase from $" + walletBalanceBefore + " but was $" + walletBalanceAfter);
    }

    @When("I select the debit card tab")
    public void iSelectTheDebitCardTab() {
        payments().selectDebitCardTab();
    }

    @Then("the card number, expiry, and CVV fields should be present")
    public void theCardNumberExpiryAndCvvFieldsShouldBePresent() {
        assertTrue(payments().areDebitCardFieldsDisplayed(), "Expected the debit card fields to be displayed");
    }
}

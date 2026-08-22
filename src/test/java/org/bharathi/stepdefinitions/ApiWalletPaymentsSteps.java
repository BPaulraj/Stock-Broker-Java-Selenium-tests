package org.bharathi.stepdefinitions;

import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.bharathi.api.ApiContext;
import org.bharathi.api.PaymentsApi;
import org.bharathi.api.WalletApi;

public class ApiWalletPaymentsSteps {

    @When("I request my wallet balance via the API")
    public void iRequestMyWalletBalanceViaTheApi() {
        Response response = WalletApi.getBalance(ApiContext.getAccessToken());
        ApiContext.setLastResponse(response);
    }

    @When("I add funds via the API using bank transfer of amount {double} to account {string} with IFSC {string}")
    public void iAddFundsViaTheApiUsingBankTransfer(double amount, String accountNumber, String ifsc) {
        Response response = PaymentsApi.addFundsByBankTransfer(ApiContext.getAccessToken(), amount, accountNumber, ifsc);
        ApiContext.setLastResponse(response);
    }

    @When("I add funds via the API using debit card of amount {double} with card number {string}, expiry {string} and cvv {string}")
    public void iAddFundsViaTheApiUsingDebitCard(double amount, String cardNumber, String expiry, String cvv) {
        Response response = PaymentsApi.addFundsByDebitCard(ApiContext.getAccessToken(), amount, cardNumber, expiry, cvv);
        ApiContext.setLastResponse(response);
    }
}

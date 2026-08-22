package org.bharathi.stepdefinitions;

import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.bharathi.api.ApiContext;
import org.bharathi.api.PortfolioApi;

public class ApiPortfolioSteps {

    @When("I request my portfolio summary via the API")
    public void iRequestMyPortfolioSummaryViaTheApi() {
        Response response = PortfolioApi.getSummary(ApiContext.getAccessToken());
        ApiContext.setLastResponse(response);
    }

    @When("I request my holdings via the API")
    public void iRequestMyHoldingsViaTheApi() {
        Response response = PortfolioApi.getHoldings(ApiContext.getAccessToken());
        ApiContext.setLastResponse(response);
    }
}

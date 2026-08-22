package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.bharathi.api.ApiContext;
import org.bharathi.api.CompaniesApi;
import org.bharathi.api.TradesApi;

import java.util.List;
import java.util.Map;

public class ApiCompaniesTradesSteps {

    @When("I list companies via the API")
    public void iListCompaniesViaTheApi() {
        Response response = CompaniesApi.list(ApiContext.getAccessToken());
        ApiContext.setLastResponse(response);
    }

    @When("I search companies via the API for {string}")
    public void iSearchCompaniesViaTheApiFor(String query) {
        Response response = CompaniesApi.search(ApiContext.getAccessToken(), query);
        ApiContext.setLastResponse(response);
    }

    @Given("I note the company id for ticker {string} from the API")
    public void iNoteTheCompanyIdForTickerFromTheApi(String ticker) {
        Response response = CompaniesApi.search(ApiContext.getAccessToken(), ticker);
        ApiContext.setLastResponse(response);
        List<Map<String, Object>> companies = response.jsonPath().getList("$");
        Map<String, Object> match = companies.stream()
                .filter(c -> ticker.equals(c.get("ticker")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No company found for ticker " + ticker));
        ApiContext.setLastCompanyId(String.valueOf(match.get("id")));
    }

    @When("I place a buy order via the API for {int} share of the noted company")
    public void iPlaceABuyOrderViaTheApiForShareOfTheNotedCompany(int quantity) {
        Response response = TradesApi.placeTrade(ApiContext.getAccessToken(), ApiContext.getLastCompanyId(), "BUY", quantity);
        recordTradeResponse(response);
    }

    @When("I place a sell order via the API for {int} share of the noted company")
    public void iPlaceASellOrderViaTheApiForShareOfTheNotedCompany(int quantity) {
        Response response = TradesApi.placeTrade(ApiContext.getAccessToken(), ApiContext.getLastCompanyId(), "SELL", quantity);
        recordTradeResponse(response);
    }

    @When("I place a buy order via the API for {int} share of the noted company without an access token")
    public void iPlaceABuyOrderViaTheApiForShareOfTheNotedCompanyWithoutAnAccessToken(int quantity) {
        Response response = TradesApi.placeTrade("", ApiContext.getLastCompanyId(), "BUY", quantity);
        ApiContext.setLastResponse(response);
    }

    @When("I list my trades via the API")
    public void iListMyTradesViaTheApi() {
        Response response = TradesApi.list(ApiContext.getAccessToken());
        ApiContext.setLastResponse(response);
    }

    private void recordTradeResponse(Response response) {
        ApiContext.setLastResponse(response);
        if (response.getStatusCode() == 201) {
            ApiContext.setLastTradeId(response.jsonPath().getString("id"));
            ApiContext.setLastInvoiceId(response.jsonPath().getString("invoiceId"));
        }
    }
}

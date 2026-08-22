package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.bharathi.api.ApiContext;
import org.bharathi.api.InvoicesApi;

import static org.testng.Assert.assertEquals;

public class ApiInvoicesSteps {

    @When("I fetch the invoice for the noted trade via the API")
    public void iFetchTheInvoiceForTheNotedTradeViaTheApi() {
        Response response = InvoicesApi.getByTrade(ApiContext.getAccessToken(), ApiContext.getLastTradeId());
        ApiContext.setLastResponse(response);
    }

    @When("I fetch the invoice for the noted trade via the API using the second account's token")
    public void iFetchTheInvoiceForTheNotedTradeViaTheApiUsingTheSecondAccountsToken() {
        Response response = InvoicesApi.getByTrade(ApiContext.getSecondaryAccessToken(), ApiContext.getLastTradeId());
        ApiContext.setLastResponse(response);
    }

    @When("I download the noted invoice's PDF via the API")
    public void iDownloadTheNotedInvoicesPdfViaTheApi() {
        Response response = InvoicesApi.getPdf(ApiContext.getAccessToken(), ApiContext.getLastInvoiceId());
        ApiContext.setLastResponse(response);
    }

    @Then("the API response should be a PDF document")
    public void theApiResponseShouldBeAPdfDocument() {
        String contentType = ApiContext.getLastResponse().getContentType();
        assertEquals(contentType, "application/pdf", "Expected a PDF response but got content type: " + contentType);
        byte[] body = ApiContext.getLastResponse().getBody().asByteArray();
        String header = new String(body, 0, Math.min(5, body.length), java.nio.charset.StandardCharsets.US_ASCII);
        assertEquals(header, "%PDF-", "Expected the response body to start with the PDF magic bytes");
    }
}

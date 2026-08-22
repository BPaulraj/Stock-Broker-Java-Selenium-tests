package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.bharathi.api.ApiContext;
import org.bharathi.api.InboxApi;

public class ApiInboxSteps {

    @When("I list my inbox messages via the API")
    public void iListMyInboxMessagesViaTheApi() {
        Response response = InboxApi.list(ApiContext.getAccessToken());
        ApiContext.setLastResponse(response);
    }

    @Given("I note the first inbox message id via the API")
    public void iNoteTheFirstInboxMessageIdViaTheApi() {
        Response response = InboxApi.list(ApiContext.getAccessToken());
        ApiContext.setLastResponse(response);
        String id = response.jsonPath().getString("[0].id");
        if (id == null) {
            throw new IllegalStateException("Expected at least one inbox message but the inbox was empty");
        }
        ApiContext.setLastMessageId(id);
    }

    @When("I fetch the noted inbox message via the API")
    public void iFetchTheNotedInboxMessageViaTheApi() {
        Response response = InboxApi.getById(ApiContext.getAccessToken(), ApiContext.getLastMessageId());
        ApiContext.setLastResponse(response);
    }

    @When("I fetch inbox message {string} via the API")
    public void iFetchInboxMessageViaTheApi(String messageId) {
        Response response = InboxApi.getById(ApiContext.getAccessToken(), messageId);
        ApiContext.setLastResponse(response);
    }
}

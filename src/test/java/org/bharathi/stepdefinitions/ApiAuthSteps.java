package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.bharathi.api.ApiContext;
import org.bharathi.api.SessionsApi;
import org.bharathi.api.UsersApi;
import org.json.JSONObject;

public class ApiAuthSteps {

    private static String uniqueEmail() {
        return "claude-api-test-" + System.currentTimeMillis() + "-" + System.nanoTime() + "@example.com";
    }

    private static Response registerFreshAccount() {
        String email = uniqueEmail();
        String password = "TestPass123!";
        Response response = UsersApi.register("Claude API Test User", email, "9999999999", password);
        ApiContext.setLastResponse(response);
        if (response.getStatusCode() == 201) {
            ApiContext.setAccountEmail(email);
            ApiContext.setAccountPassword(password);
            ApiContext.setAccessToken(response.jsonPath().getString("accessToken"));
        }
        return response;
    }

    @Given("I have a fresh registered account via the API")
    public void iHaveAFreshRegisteredAccountViaTheApi() {
        Response response = registerFreshAccount();
        if (response.getStatusCode() != 201) {
            throw new IllegalStateException("Failed to register a fresh test account: " + response.getBody().asString());
        }
    }

    @Given("I have a second fresh registered account via the API")
    public void iHaveASecondFreshRegisteredAccountViaTheApi() {
        String email = uniqueEmail();
        String password = "TestPass123!";
        Response response = UsersApi.register("Claude API Second User", email, "9999999999", password);
        ApiContext.setLastResponse(response);
        if (response.getStatusCode() != 201) {
            throw new IllegalStateException("Failed to register a second test account: " + response.getBody().asString());
        }
        ApiContext.setSecondaryAccessToken(response.jsonPath().getString("accessToken"));
    }

    @When("I register a new account via the API with email {string} and password {string}")
    public void iRegisterANewAccountViaTheApiWithEmailAndPassword(String email, String password) {
        Response response = UsersApi.register("Claude API Test User", email, "9999999999", password);
        ApiContext.setLastResponse(response);
    }

    @When("I register via the API with my existing account's email")
    public void iRegisterViaTheApiWithMyExistingAccountsEmail() {
        Response response = UsersApi.register("Claude API Test User", ApiContext.getAccountEmail(), "9999999999", "TestPass123!");
        ApiContext.setLastResponse(response);
    }

    @When("I log in via the API with my registered account's credentials")
    public void iLogInViaTheApiWithMyRegisteredAccountsCredentials() {
        Response response = SessionsApi.login(ApiContext.getAccountEmail(), ApiContext.getAccountPassword());
        ApiContext.setLastResponse(response);
        if (response.getStatusCode() == 201) {
            ApiContext.setAccessToken(response.jsonPath().getString("accessToken"));
        }
    }

    @When("I log in via the API with email {string} and password {string}")
    public void iLogInViaTheApiWithEmailAndPassword(String email, String password) {
        Response response = SessionsApi.login(email, password);
        ApiContext.setLastResponse(response);
    }

    @When("I request my profile via the API")
    public void iRequestMyProfileViaTheApi() {
        Response response = UsersApi.getMe(ApiContext.getAccessToken());
        ApiContext.setLastResponse(response);
    }

    @When("I request my profile via the API without an access token")
    public void iRequestMyProfileViaTheApiWithoutAnAccessToken() {
        Response response = UsersApi.getMe("");
        ApiContext.setLastResponse(response);
    }

    @When("I request my profile via the API with an invalid access token")
    public void iRequestMyProfileViaTheApiWithAnInvalidAccessToken() {
        Response response = UsersApi.getMe("not-a-real-token");
        ApiContext.setLastResponse(response);
    }

    @When("I update my profile via the API with name {string}")
    public void iUpdateMyProfileViaTheApiWithName(String name) {
        Response response = UsersApi.updateMe(ApiContext.getAccessToken(), new JSONObject().put("name", name));
        ApiContext.setLastResponse(response);
    }
}

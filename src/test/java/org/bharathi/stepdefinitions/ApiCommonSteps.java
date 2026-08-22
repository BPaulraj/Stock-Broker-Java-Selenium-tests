package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Then;
import org.bharathi.api.ApiContext;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Generic REST-response assertions shared across every API feature file, so each
 * resource's step-definition class only needs to define steps for triggering calls,
 * not for re-implementing the same status/field checks.
 */
public class ApiCommonSteps {

    @Then("the API response status code should be {int}")
    public void theApiResponseStatusCodeShouldBe(int expectedStatus) {
        int actualStatus = ApiContext.getLastResponse().getStatusCode();
        assertEquals(actualStatus, expectedStatus,
                "Expected status " + expectedStatus + " but got " + actualStatus + ". Body: "
                        + ApiContext.getLastResponse().getBody().asString());
    }

    @Then("the API response field {string} should equal {string}")
    public void theApiResponseFieldShouldEqual(String jsonPath, String expectedValue) {
        Object actual = ApiContext.getLastResponse().jsonPath().get(jsonPath);
        assertEquals(String.valueOf(actual), expectedValue,
                "Expected field '" + jsonPath + "' to equal '" + expectedValue + "' but was '" + actual + "'");
    }

    @Then("the API response field {string} should be greater than {double}")
    public void theApiResponseFieldShouldBeGreaterThan(String jsonPath, double lowerBound) {
        Number actual = ApiContext.getLastResponse().jsonPath().get(jsonPath);
        assertNotNull(actual, "Expected field '" + jsonPath + "' to be present");
        assertTrue(actual.doubleValue() > lowerBound,
                "Expected field '" + jsonPath + "' (" + actual + ") to be greater than " + lowerBound);
    }

    @Then("the API response field {string} should not be null")
    public void theApiResponseFieldShouldNotBeNull(String jsonPath) {
        Object actual = ApiContext.getLastResponse().jsonPath().get(jsonPath);
        assertNotNull(actual, "Expected field '" + jsonPath + "' to be present and non-null");
    }

    @Then("the API error message should contain {string}")
    public void theApiErrorMessageShouldContain(String expectedFragment) {
        String error = ApiContext.getLastResponse().jsonPath().getString("error");
        assertNotNull(error, "Expected an 'error' field in the response body");
        assertTrue(error.contains(expectedFragment),
                "Expected error message to contain '" + expectedFragment + "' but was '" + error + "'");
    }

    @Then("the API response field {string} should have a validation error mentioning {string}")
    public void theApiResponseFieldShouldHaveAValidationErrorMentioning(String field, String expectedFragment) {
        String message = ApiContext.getLastResponse().jsonPath().getString("fieldErrors." + field);
        assertNotNull(message, "Expected a fieldErrors." + field + " entry in the response body");
        assertTrue(message.contains(expectedFragment),
                "Expected validation error on '" + field + "' to contain '" + expectedFragment + "' but was '" + message + "'");
    }
}

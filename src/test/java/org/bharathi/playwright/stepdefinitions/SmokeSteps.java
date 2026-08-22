package org.bharathi.playwright.stepdefinitions;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.config.ConfigReader;
import org.bharathi.playwright.driver.PlaywrightContextManager;

import static org.testng.Assert.assertTrue;

/**
 * Playwright equivalent of {@link org.bharathi.stepdefinitions.SmokeSteps} for
 * smoke.feature. Same step text/signatures as the legacy class — only the
 * underlying driver/page plumbing changed.
 */
public class SmokeSteps {

    private Page page() {
        return PlaywrightContextManager.getPage();
    }

    @Given("the browser is open")
    public void theBrowserIsOpen() {
        page();
    }

    @Given("I open the StockBroker application")
    public void iOpenTheStockBrokerApplication() {
        page().navigate(ConfigReader.getBaseUrl());
    }

    @When("I navigate to {string}")
    public void iNavigateTo(String url) {
        page().navigate(url);
    }

    @Then("the page title should contain {string}")
    public void thePageTitleShouldContain(String expected) {
        assertTrue(page().title().contains(expected),
                "Expected title to contain '" + expected + "' but was '" + page().title() + "'");
    }
}

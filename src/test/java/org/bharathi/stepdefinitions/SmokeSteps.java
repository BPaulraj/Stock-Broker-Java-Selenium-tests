package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.config.ConfigReader;
import org.bharathi.driver.DriverManager;
import org.openqa.selenium.WebDriver;

import static org.testng.Assert.assertTrue;

public class SmokeSteps {

    private WebDriver driver() {
        return DriverManager.getDriver();
    }

    @Given("the browser is open")
    public void theBrowserIsOpen() {
        driver();
    }

    @Given("I open the StockBroker application")
    public void iOpenTheStockBrokerApplication() {
        driver().get(ConfigReader.getBaseUrl());
    }

    @When("I navigate to {string}")
    public void iNavigateTo(String url) {
        driver().get(url);
    }

    @Then("the page title should contain {string}")
    public void thePageTitleShouldContain(String expected) {
        assertTrue(driver().getTitle().contains(expected),
                "Expected title to contain '" + expected + "' but was '" + driver().getTitle() + "'");
    }
}

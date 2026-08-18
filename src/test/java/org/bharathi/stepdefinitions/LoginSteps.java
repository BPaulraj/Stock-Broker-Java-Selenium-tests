package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.config.ConfigReader;
import org.bharathi.driver.DriverManager;
import org.bharathi.pages.DashboardPage;
import org.bharathi.pages.LoginPage;

import java.time.Duration;

import static org.testng.Assert.assertTrue;

public class LoginSteps {

    @When("I log in with a valid email and password")
    public void iLogInWithAValidEmailAndPassword() {
        LoginPage loginPage = new LoginPage(DriverManager.getDriver());
        loginPage.enterEmail(ConfigReader.getTestEmail())
                .enterPassword(ConfigReader.getTestPassword())
                .submit();
    }

    @Then("I should be logged in")
    public void iShouldBeLoggedIn() {
        boolean loggedIn = new DashboardPage(DriverManager.getDriver()).isDisplayed(Duration.ofSeconds(20));
        assertTrue(loggedIn, "Expected the dashboard (Log out button) to appear after logging in");
    }
}

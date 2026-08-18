package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.driver.DriverManager;
import org.bharathi.pages.LoginPage;
import org.bharathi.pages.RegisterPage;

import static org.testng.Assert.assertTrue;

public class RegisterSteps {

    @Given("I go to the sign up page")
    public void iGoToTheSignUpPage() {
        new LoginPage(DriverManager.getDriver()).goToSignUp();
    }

    @When("I register with a new, unique account")
    public void iRegisterWithANewUniqueAccount() {
        String uniqueEmail = "claude-test-" + System.currentTimeMillis() + "@example.com";
        new RegisterPage(DriverManager.getDriver())
                .enterFullName("Claude Test User")
                .enterEmail(uniqueEmail)
                .enterPhone("9999999999")
                .enterPassword("TestPass123!")
                .createAccount();
    }

    @When("I follow the log in link")
    public void iFollowTheLogInLink() {
        new RegisterPage(DriverManager.getDriver()).goToLogIn();
    }

    @Then("the login page should be displayed")
    public void theLoginPageShouldBeDisplayed() {
        assertTrue(new LoginPage(DriverManager.getDriver()).isDisplayed(), "Expected the login page to be displayed");
    }
}

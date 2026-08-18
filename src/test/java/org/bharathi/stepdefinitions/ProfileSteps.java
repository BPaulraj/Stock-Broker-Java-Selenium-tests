package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.config.ConfigReader;
import org.bharathi.driver.DriverManager;
import org.bharathi.pages.DashboardPage;
import org.bharathi.pages.ProfilePage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ProfileSteps {

    private ProfilePage profile() {
        return new ProfilePage(DriverManager.getDriver());
    }

    @Given("I go to the profile page")
    public void iGoToTheProfilePage() {
        new DashboardPage(DriverManager.getDriver()).goToProfile();
    }

    @Then("the Profile page should be displayed")
    public void theProfilePageShouldBeDisplayed() {
        assertEquals(profile().getHeading(), "Profile");
    }

    @Then("the account email should match the logged-in user's email")
    public void theAccountEmailShouldMatchTheLoggedInUsersEmail() {
        assertEquals(profile().getEmail(), ConfigReader.getTestEmail());
    }

    @When("I update the phone number to {string}")
    public void iUpdateThePhoneNumberTo(String phone) {
        profile().setPhone(phone);
    }

    @When("I save the profile changes")
    public void iSaveTheProfileChanges() {
        profile().saveChanges();
    }

    @Then("the profile status message should confirm the update")
    public void theProfileStatusMessageShouldConfirmTheUpdate() {
        assertTrue(profile().getStatusMessage().isPresent(), "Expected a status message after saving profile changes");
        assertTrue(profile().getStatusMessage().get().toLowerCase().contains("updated"),
                "Expected the status message to confirm the update but was: " + profile().getStatusMessage());
    }
}

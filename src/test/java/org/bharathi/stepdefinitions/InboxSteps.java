package org.bharathi.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.driver.DriverManager;
import org.bharathi.pages.DashboardPage;
import org.bharathi.pages.InboxPage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

public class InboxSteps {

    private String markReadButtonTextBefore;

    private InboxPage inbox() {
        return new InboxPage(DriverManager.getDriver());
    }

    @Given("I go to the inbox page")
    public void iGoToTheInboxPage() {
        new DashboardPage(DriverManager.getDriver()).goToInbox();
    }

    @Then("the Inbox page should be displayed")
    public void theInboxPageShouldBeDisplayed() {
        assertEquals(inbox().getHeading(), "Inbox");
    }

    @Given("I select the first message in the inbox")
    public void iSelectTheFirstMessageInTheInbox() {
        String subject = inbox().getMessages().get(0).subject();
        inbox().selectMessage(subject);
    }

    @Then("the message detail should show a non-empty subject and body")
    public void theMessageDetailShouldShowANonEmptySubjectAndBody() {
        assertFalse(inbox().getDetailSubject().isBlank(), "Expected the detail subject to be non-empty");
        assertFalse(inbox().getDetailBody().isBlank(), "Expected the detail body to be non-empty");
    }

    @When("I toggle the message's read state")
    public void iToggleTheMessagesReadState() {
        markReadButtonTextBefore = inbox().getMarkReadButtonText();
        inbox().toggleReadState();
    }

    @Then("the mark-read button label should have changed")
    public void theMarkReadButtonLabelShouldHaveChanged() {
        assertNotEquals(inbox().getMarkReadButtonText(), markReadButtonTextBefore,
                "Expected the mark-read button label to change after toggling");
    }
}

package org.bharathi.playwright.stepdefinitions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bharathi.config.ConfigReader;
import org.bharathi.playwright.driver.PlaywrightContextManager;
import org.bharathi.playwright.pages.LoginPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Playwright equivalent of {@link org.bharathi.stepdefinitions.LoginSteps} for
 * login.feature. Same step text/signatures as the legacy class — only the
 * underlying driver/page plumbing changed.
 */
public class LoginSteps {

    @When("I log in with a valid email and password")
    public void iLogInWithAValidEmailAndPassword() {
        LoginPage loginPage = new LoginPage(PlaywrightContextManager.getPage());
        loginPage.enterEmail(ConfigReader.getTestEmail())
                .enterPassword(ConfigReader.getTestPassword())
                .submit();
    }

    @Then("I should be logged in")
    public void iShouldBeLoggedIn() {
        // NOTE (batch-scope flag, see batch report): the legacy LoginSteps verifies this
        // via `new DashboardPage(driver).isDisplayed(...)`, but DashboardPage conversion
        // is phase-2/batch-1's job per the Migration Plan, not this batch's. Rather than
        // reaching ahead and porting/creating a Playwright DashboardPage here, this
        // checks the same single element the legacy DashboardPage.isDisplayed() checks
        // (the header "Log out" button) inline. When phase-2/batch-1 lands, this should
        // be revisited to call the real Playwright DashboardPage instead.
        Page page = PlaywrightContextManager.getPage();
        Locator logoutButton = page.locator("header button", new Page.LocatorOptions().setHasText("Log out"));
        assertThat(logoutButton).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(20000));
    }
}

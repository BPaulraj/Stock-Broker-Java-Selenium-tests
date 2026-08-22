package org.bharathi.playwright.hooks;

import io.cucumber.java.AfterAll;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.bharathi.playwright.driver.PlaywrightContextManager;

/**
 * Playwright equivalent of {@link org.bharathi.hooks.Hooks}: same hook shape and
 * same tag-scoping, opens/closes a BrowserContext instead of a WebDriver. The
 * legacy Hooks class's "@api" branch (ApiContext reset) has no equivalent here
 * because the REST Assured API layer is out of scope for this migration (Gate D,
 * Open Question 1) — this glue package never sees an @api-tagged scenario.
 */
public class Hooks {

    @Before("not @api")
    public void setUp() {
        PlaywrightContextManager.getPage();
    }

    @After("not @api")
    public void tearDown() {
        PlaywrightContextManager.closeContext();
    }

    @AfterAll
    public static void shutdownBrowser() {
        PlaywrightContextManager.shutdown();
    }
}

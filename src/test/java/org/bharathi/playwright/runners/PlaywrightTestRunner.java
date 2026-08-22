package org.bharathi.playwright.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Playwright-batch equivalent of {@link org.bharathi.runners.TestRunner}, scoped
 * to exactly the feature files converted in phase-1/batch-1. Points at the same,
 * untouched legacy .feature files (login.feature, smoke.feature) — the Gherkin
 * doesn't change, only the glue implementing it. As later batches convert more
 * pages, their feature files get added to this runner's {@code features} list.
 */
@CucumberOptions(
        features = {
                "src/test/resources/features/login.feature",
                "src/test/resources/features/smoke.feature"
        },
        glue = {"org.bharathi.playwright.stepdefinitions", "org.bharathi.playwright.hooks"},
        plugin = {"pretty", "summary"}
)
public class PlaywrightTestRunner extends AbstractTestNGCucumberTests {
}

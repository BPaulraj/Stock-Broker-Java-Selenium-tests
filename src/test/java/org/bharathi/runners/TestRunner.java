package org.bharathi.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"org.bharathi.stepdefinitions", "org.bharathi.hooks"},
        plugin = {"pretty", "summary"}
)
public class TestRunner extends AbstractTestNGCucumberTests {
}

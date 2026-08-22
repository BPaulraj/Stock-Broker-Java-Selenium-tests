package org.bharathi.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.bharathi.api.ApiContext;
import org.bharathi.driver.DriverManager;

public class Hooks {

    @Before("not @api")
    public void setUp() {
        DriverManager.getDriver();
    }

    @After("not @api")
    public void tearDown() {
        DriverManager.quitDriver();
    }

    @After("@api")
    public void resetApiContext() {
        ApiContext.reset();
    }
}

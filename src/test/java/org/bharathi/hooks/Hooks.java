package org.bharathi.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.bharathi.driver.DriverManager;

public class Hooks {

    @Before
    public void setUp() {
        DriverManager.getDriver();
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
}

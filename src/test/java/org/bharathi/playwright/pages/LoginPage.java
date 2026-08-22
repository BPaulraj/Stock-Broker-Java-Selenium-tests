package org.bharathi.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Playwright equivalent of {@link org.bharathi.pages.LoginPage} — 1:1 structural
 * port per the conversion skill's POM mapping. This page uses stable
 * {@code #email}/{@code #password}/{@code button[type='submit']} locators (per
 * CLAUDE.md), so no text-matching/normalize-space() complications apply here.
 */
public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    private Locator emailInput() {
        return page.locator("#email");
    }

    private Locator passwordInput() {
        return page.locator("#password");
    }

    private Locator submitButton() {
        return page.locator("button[type='submit']");
    }

    private Locator signUpLink() {
        return page.locator("a[href='/register']");
    }

    public LoginPage enterEmail(String email) {
        emailInput().fill(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        passwordInput().fill(password);
        return this;
    }

    public void submit() {
        submitButton().click();
    }

    public boolean isDisplayed() {
        return emailInput().isVisible();
    }

    public void goToSignUp() {
        signUpLink().click();
    }
}

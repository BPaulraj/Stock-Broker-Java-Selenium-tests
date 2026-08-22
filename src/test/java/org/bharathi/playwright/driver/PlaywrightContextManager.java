package org.bharathi.playwright.driver;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Playwright equivalent of {@link org.bharathi.driver.DriverManager}: same
 * ThreadLocal-per-scenario shape, different payload. One process-wide
 * {@link Playwright}/{@link Browser} pair is created lazily on first use and
 * shared across threads; each thread/scenario gets its own {@link BrowserContext}
 * (+ {@link Page}) for isolation, which is cheaper than Selenium's one-driver-
 * process-per-thread model.
 *
 * Chrome-only for now, matching the legacy suite's hardcoded ChromeDriver — not a
 * regression, just not yet extended to cross-browser config.
 */
public final class PlaywrightContextManager {

    private static volatile Playwright playwright;
    private static volatile Browser browser;

    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private PlaywrightContextManager() {
    }

    private static synchronized void ensureBrowser() {
        if (browser == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch();
        }
    }

    /**
     * Returns this thread's {@link Page}, lazily creating the shared {@link Browser}
     * and a fresh {@link BrowserContext}+{@link Page} for this thread if needed.
     */
    public static Page getPage() {
        if (PAGE.get() == null) {
            ensureBrowser();
            BrowserContext context = browser.newContext();
            CONTEXT.set(context);
            PAGE.set(context.newPage());
        }
        return PAGE.get();
    }

    /**
     * Closes this thread's BrowserContext (and its Page). Equivalent of
     * DriverManager.quitDriver(), called per scenario rather than per suite.
     */
    public static void closeContext() {
        BrowserContext context = CONTEXT.get();
        if (context != null) {
            context.close();
            CONTEXT.remove();
        }
        PAGE.remove();
    }

    /**
     * Closes the process-wide Browser/Playwright. Call once after the whole suite
     * finishes (e.g. from a Cucumber {@code @AfterAll} hook), not per scenario.
     */
    public static synchronized void shutdown() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}

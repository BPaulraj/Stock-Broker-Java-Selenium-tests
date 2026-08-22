package org.bharathi.api;

import io.restassured.response.Response;

/**
 * ThreadLocal state shared across API step-definition classes within one scenario,
 * mirroring how {@link org.bharathi.driver.DriverManager} shares one WebDriver across
 * UI step-definition classes. Cucumber creates a separate instance of each step-def
 * class per scenario, so cross-class state (access token, last response, ids picked up
 * in one step and asserted on in another) has to live here rather than in instance fields.
 */
public final class ApiContext {

    private static final ThreadLocal<String> ACCESS_TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<String> SECONDARY_ACCESS_TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<String> ACCOUNT_EMAIL = new ThreadLocal<>();
    private static final ThreadLocal<String> ACCOUNT_PASSWORD = new ThreadLocal<>();
    private static final ThreadLocal<Response> LAST_RESPONSE = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_COMPANY_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_TRADE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_INVOICE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_MESSAGE_ID = new ThreadLocal<>();

    private ApiContext() {
    }

    public static void setAccessToken(String token) {
        ACCESS_TOKEN.set(token);
    }

    public static String getAccessToken() {
        return ACCESS_TOKEN.get();
    }

    public static void setSecondaryAccessToken(String token) {
        SECONDARY_ACCESS_TOKEN.set(token);
    }

    public static String getSecondaryAccessToken() {
        return SECONDARY_ACCESS_TOKEN.get();
    }

    public static void setAccountEmail(String email) {
        ACCOUNT_EMAIL.set(email);
    }

    public static String getAccountEmail() {
        return ACCOUNT_EMAIL.get();
    }

    public static void setAccountPassword(String password) {
        ACCOUNT_PASSWORD.set(password);
    }

    public static String getAccountPassword() {
        return ACCOUNT_PASSWORD.get();
    }

    public static void setLastResponse(Response response) {
        LAST_RESPONSE.set(response);
    }

    public static Response getLastResponse() {
        Response response = LAST_RESPONSE.get();
        if (response == null) {
            throw new IllegalStateException("No API response recorded yet for this scenario");
        }
        return response;
    }

    public static void setLastCompanyId(String companyId) {
        LAST_COMPANY_ID.set(companyId);
    }

    public static String getLastCompanyId() {
        return LAST_COMPANY_ID.get();
    }

    public static void setLastTradeId(String tradeId) {
        LAST_TRADE_ID.set(tradeId);
    }

    public static String getLastTradeId() {
        return LAST_TRADE_ID.get();
    }

    public static void setLastInvoiceId(String invoiceId) {
        LAST_INVOICE_ID.set(invoiceId);
    }

    public static String getLastInvoiceId() {
        return LAST_INVOICE_ID.get();
    }

    public static void setLastMessageId(String messageId) {
        LAST_MESSAGE_ID.set(messageId);
    }

    public static String getLastMessageId() {
        return LAST_MESSAGE_ID.get();
    }

    public static void reset() {
        ACCESS_TOKEN.remove();
        SECONDARY_ACCESS_TOKEN.remove();
        ACCOUNT_EMAIL.remove();
        ACCOUNT_PASSWORD.remove();
        LAST_RESPONSE.remove();
        LAST_COMPANY_ID.remove();
        LAST_TRADE_ID.remove();
        LAST_INVOICE_ID.remove();
        LAST_MESSAGE_ID.remove();
    }
}

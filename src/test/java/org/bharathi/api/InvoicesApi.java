package org.bharathi.api;

import io.restassured.response.Response;

public final class InvoicesApi {

    private InvoicesApi() {
    }

    public static Response getByTrade(String accessToken, String tradeId) {
        return ApiClient.authenticatedRequest(accessToken).get("/invoices/by-trade/" + tradeId);
    }

    public static Response getPdf(String accessToken, String invoiceId) {
        return ApiClient.authenticatedRequest(accessToken).get("/invoices/" + invoiceId + "/pdf");
    }
}

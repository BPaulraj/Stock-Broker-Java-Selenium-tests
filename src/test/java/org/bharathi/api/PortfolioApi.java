package org.bharathi.api;

import io.restassured.response.Response;

public final class PortfolioApi {

    private PortfolioApi() {
    }

    public static Response getSummary(String accessToken) {
        return ApiClient.authenticatedRequest(accessToken).get("/portfolio");
    }

    public static Response getHoldings(String accessToken) {
        return ApiClient.authenticatedRequest(accessToken).get("/portfolio/holdings");
    }
}

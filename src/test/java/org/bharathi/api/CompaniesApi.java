package org.bharathi.api;

import io.restassured.response.Response;

public final class CompaniesApi {

    private CompaniesApi() {
    }

    public static Response list(String accessToken) {
        return ApiClient.authenticatedRequest(accessToken).get("/companies");
    }

    public static Response search(String accessToken, String query) {
        return ApiClient.authenticatedRequest(accessToken).queryParam("search", query).get("/companies");
    }
}

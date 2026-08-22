package org.bharathi.api;

import io.restassured.response.Response;

public final class InboxApi {

    private InboxApi() {
    }

    public static Response list(String accessToken) {
        return ApiClient.authenticatedRequest(accessToken).get("/inbox");
    }

    public static Response getById(String accessToken, String id) {
        return ApiClient.authenticatedRequest(accessToken).get("/inbox/" + id);
    }
}

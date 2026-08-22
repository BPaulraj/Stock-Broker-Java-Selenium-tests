package org.bharathi.api;

import io.restassured.response.Response;
import org.json.JSONObject;

public final class TradesApi {

    private TradesApi() {
    }

    public static Response list(String accessToken) {
        return ApiClient.authenticatedRequest(accessToken).get("/trades");
    }

    public static Response placeTrade(String accessToken, String companyId, String type, int quantity) {
        JSONObject body = new JSONObject()
                .put("companyId", companyId)
                .put("type", type)
                .put("quantity", quantity);
        return ApiClient.authenticatedRequest(accessToken).body(body.toString()).post("/trades");
    }
}

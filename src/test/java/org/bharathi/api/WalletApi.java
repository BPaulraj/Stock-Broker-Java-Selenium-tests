package org.bharathi.api;

import io.restassured.response.Response;

public final class WalletApi {

    private WalletApi() {
    }

    public static Response getBalance(String accessToken) {
        return ApiClient.authenticatedRequest(accessToken).get("/wallet/balance");
    }
}

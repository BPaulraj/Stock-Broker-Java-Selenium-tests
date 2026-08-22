package org.bharathi.api;

import io.restassured.response.Response;
import org.json.JSONObject;

public final class SessionsApi {

    private SessionsApi() {
    }

    public static Response login(String email, String password) {
        JSONObject body = new JSONObject().put("email", email).put("password", password);
        return ApiClient.request().body(body.toString()).post("/sessions");
    }
}

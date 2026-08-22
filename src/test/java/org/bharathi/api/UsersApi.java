package org.bharathi.api;

import io.restassured.response.Response;
import org.json.JSONObject;

public final class UsersApi {

    private UsersApi() {
    }

    public static Response register(String name, String email, String phone, String password) {
        JSONObject body = new JSONObject()
                .put("name", name)
                .put("email", email)
                .put("phone", phone)
                .put("password", password);
        return ApiClient.request().body(body.toString()).post("/users");
    }

    public static Response getMe(String accessToken) {
        return ApiClient.authenticatedRequest(accessToken).get("/users/me");
    }

    public static Response updateMe(String accessToken, JSONObject body) {
        return ApiClient.authenticatedRequest(accessToken).body(body.toString()).put("/users/me");
    }
}

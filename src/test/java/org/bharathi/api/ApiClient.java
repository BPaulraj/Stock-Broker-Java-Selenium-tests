package org.bharathi.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.bharathi.config.ConfigReader;

public final class ApiClient {

    private ApiClient() {
    }

    public static RequestSpecification request() {
        return RestAssured.given()
                .baseUri(ConfigReader.getApiBaseUrl())
                .contentType(ContentType.JSON);
    }

    public static RequestSpecification authenticatedRequest(String accessToken) {
        return request().header("Authorization", "Bearer " + accessToken);
    }
}

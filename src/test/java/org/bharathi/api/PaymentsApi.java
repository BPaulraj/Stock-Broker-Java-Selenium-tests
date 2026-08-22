package org.bharathi.api;

import io.restassured.response.Response;
import org.json.JSONObject;

public final class PaymentsApi {

    private PaymentsApi() {
    }

    public static Response addFundsByBankTransfer(String accessToken, double amount, String accountNumber, String ifsc) {
        JSONObject body = new JSONObject()
                .put("method", "BANK_TRANSFER")
                .put("amount", amount)
                .put("accountNumber", accountNumber)
                .put("ifsc", ifsc);
        return ApiClient.authenticatedRequest(accessToken).body(body.toString()).post("/payments");
    }

    public static Response addFundsByDebitCard(String accessToken, double amount, String cardNumber, String expiry, String cvv) {
        JSONObject body = new JSONObject()
                .put("method", "DEBIT_CARD")
                .put("amount", amount)
                .put("cardNumber", cardNumber)
                .put("expiry", expiry)
                .put("cvv", cvv);
        return ApiClient.authenticatedRequest(accessToken).body(body.toString()).post("/payments");
    }
}

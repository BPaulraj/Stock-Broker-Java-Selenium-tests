@api
Feature: REST API - Wallet and payments
  Covers GET /wallet/balance and POST /payments on the versioned REST API.
  Every scenario runs against its own freshly registered account so wallet
  balance assertions never depend on drift from other tests.

  Background:
    Given I have a fresh registered account via the API

  Scenario: A new account starts with a zero wallet balance
    When I request my wallet balance via the API
    Then the API response status code should be 200
    And the API response field "balance" should equal "0"

  Scenario: Adding funds via bank transfer credits the wallet
    When I add funds via the API using bank transfer of amount 250.0 to account "123456789012" with IFSC "HDFC0123456"
    Then the API response status code should be 201
    And the API response field "balance" should equal "250"

  Scenario: Adding funds via debit card credits the wallet
    When I add funds via the API using debit card of amount 100.0 with card number "4111111111111111", expiry "12/30" and cvv "123"
    Then the API response status code should be 201
    And the API response field "balance" should equal "100"

  Scenario: Adding funds with a card number that fails validation is rejected
    When I add funds via the API using debit card of amount 100.0 with card number "4111111111111112", expiry "12/30" and cvv "123"
    Then the API response status code should be 400
    And the API response field "cardNumber" should have a validation error mentioning "failed validation"

  Scenario: Adding a negative amount is rejected
    When I add funds via the API using bank transfer of amount -50.0 to account "123456789012" with IFSC "HDFC0123456"
    Then the API response status code should be 400
    And the API response field "amount" should have a validation error mentioning "greater than 0"

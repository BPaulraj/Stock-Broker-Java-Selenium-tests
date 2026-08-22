@api
Feature: REST API - Invoices
  Covers GET /invoices/by-trade/:tradeId and GET /invoices/:invoiceId/pdf on
  the versioned REST API, including tenant isolation (a user must not be able
  to fetch another user's invoice by guessing/reusing a trade id).

  Background:
    Given I have a fresh registered account via the API
    And I note the company id for ticker "AAPL" from the API
    And I add funds via the API using bank transfer of amount 100000.0 to account "123456789012" with IFSC "HDFC0123456"
    And I place a buy order via the API for 1 share of the noted company

  Scenario: Fetching the invoice for a completed trade returns its details and a PDF link
    When I fetch the invoice for the noted trade via the API
    Then the API response status code should be 200
    And the API response field "ticker" should equal "AAPL"
    And the API response field "pdfUrl" should not be null

  Scenario: Downloading the invoice PDF returns a real PDF document
    When I download the noted invoice's PDF via the API
    Then the API response status code should be 200
    And the API response should be a PDF document

  Scenario: A different account cannot fetch another user's invoice by trade id
    Given I have a second fresh registered account via the API
    When I fetch the invoice for the noted trade via the API using the second account's token
    Then the API response status code should be 404

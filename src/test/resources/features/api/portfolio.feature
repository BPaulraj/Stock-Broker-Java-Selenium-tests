@api
Feature: REST API - Portfolio
  Covers GET /portfolio and GET /portfolio/holdings on the versioned REST API.

  Background:
    Given I have a fresh registered account via the API

  Scenario: A new account's portfolio has zero stock value and no holdings
    When I request my portfolio summary via the API
    Then the API response status code should be 200
    And the API response field "stockValue" should equal "0"
    And the API response field "holdings" should equal "[]"

  Scenario: The portfolio reflects a completed purchase
    Given I note the company id for ticker "AAPL" from the API
    And I add funds via the API using bank transfer of amount 100000.0 to account "123456789012" with IFSC "HDFC0123456"
    And I place a buy order via the API for 1 share of the noted company
    When I request my portfolio summary via the API
    Then the API response status code should be 200
    And the API response field "stockValue" should be greater than 0.0
    And the API response field "holdings[0].ticker" should equal "AAPL"

  Scenario: The holdings endpoint lists a purchased company
    Given I note the company id for ticker "AAPL" from the API
    And I add funds via the API using bank transfer of amount 100000.0 to account "123456789012" with IFSC "HDFC0123456"
    And I place a buy order via the API for 1 share of the noted company
    When I request my holdings via the API
    Then the API response status code should be 200
    And the API response field "[0].ticker" should equal "AAPL"
    And the API response field "[0].quantity" should equal "1"

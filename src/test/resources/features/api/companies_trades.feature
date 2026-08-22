@api
Feature: REST API - Companies and trades
  Covers GET /companies and GET+POST /trades on the versioned REST API.
  Buy/sell scenarios execute real trades against a freshly registered
  account, matching this project's "execute mutating actions for real"
  convention already used by the UI trade.feature.

  Background:
    Given I have a fresh registered account via the API

  Scenario: Listing companies returns the catalog
    When I list companies via the API
    Then the API response status code should be 200
    And the API response field "[0].ticker" should not be null

  Scenario: Searching companies filters results by ticker
    When I search companies via the API for "AAPL"
    Then the API response status code should be 200
    And the API response field "[0].ticker" should equal "AAPL"

  Scenario: Buying shares creates a completed trade and debits the wallet
    Given I note the company id for ticker "AAPL" from the API
    And I add funds via the API using bank transfer of amount 100000.0 to account "123456789012" with IFSC "HDFC0123456"
    When I place a buy order via the API for 1 share of the noted company
    Then the API response status code should be 201
    And the API response field "ticker" should equal "AAPL"
    And the API response field "type" should equal "BUY"
    And the API response field "status" should equal "COMPLETED"

  Scenario: The purchased trade appears in the trade history
    Given I note the company id for ticker "AAPL" from the API
    And I add funds via the API using bank transfer of amount 100000.0 to account "123456789012" with IFSC "HDFC0123456"
    And I place a buy order via the API for 1 share of the noted company
    When I list my trades via the API
    Then the API response status code should be 200
    And the API response field "[0].ticker" should equal "AAPL"

  Scenario: Selling shares that are not held is rejected
    Given I note the company id for ticker "AAPL" from the API
    When I place a sell order via the API for 1 share of the noted company
    Then the API response status code should be 400
    And the API error message should contain "Insufficient holdings"

  Scenario: Buying without sufficient wallet balance is rejected
    Given I note the company id for ticker "AAPL" from the API
    When I place a buy order via the API for 1 share of the noted company
    Then the API response status code should be 400
    And the API error message should contain "Insufficient wallet balance"

  Scenario: Placing a trade without authentication is rejected
    Given I note the company id for ticker "AAPL" from the API
    When I place a buy order via the API for 1 share of the noted company without an access token
    Then the API response status code should be 401

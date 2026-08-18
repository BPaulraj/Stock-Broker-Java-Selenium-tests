Feature: Trade

  Background:
    Given I open the StockBroker application
    When I log in with a valid email and password
    Then I should be logged in
    And I go to the trade page

  Scenario: Searching filters the company list
    When I search for company "NVDA"
    Then every listed company should match "NVDA"

  Scenario: Buying a share increases the holding and debits the wallet
    Given I note the wallet balance shown in the order panel for "AAPL"
    When I buy 1 share of "AAPL"
    Then the trade confirmation should mention "AAPL"
    And the trade wallet balance should have decreased

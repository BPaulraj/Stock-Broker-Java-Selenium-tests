Feature: Smoke test

  Scenario: Application loads
    Given I open the StockBroker application
    Then the page title should contain "StockBroker"

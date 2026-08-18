Feature: Payments

  Background:
    Given I open the StockBroker application
    When I log in with a valid email and password
    Then I should be logged in
    And I go to the payments page

  Scenario: Adding funds via bank transfer increases the wallet balance
    Given I note the wallet balance
    When I add funds of "10" via bank transfer
    Then the payment confirmation should mention the new balance
    And the wallet balance should have increased

  Scenario: Switching to the debit card tab shows the card fields
    When I select the debit card tab
    Then the card number, expiry, and CVV fields should be present

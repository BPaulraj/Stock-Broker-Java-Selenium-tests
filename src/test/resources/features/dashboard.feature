Feature: Dashboard

  Background:
    Given I open the StockBroker application
    When I log in with a valid email and password
    Then I should be logged in

  Scenario: Dashboard shows the account summary and holdings
    Then the welcome heading should greet the logged-in user
    And the wallet balance should be a dollar amount
    And the holdings table should list at least one holding

  Scenario: Nav links open the corresponding pages
    When I go to Trade from the nav
    Then the Trade page should be displayed
    When I go to Payments from the nav
    Then the Payments page should be displayed
    When I go to Inbox from the nav
    Then the Inbox page should be displayed
    When I go to Profile from the nav
    Then the Profile page should be displayed

  Scenario: Quick-link tiles open the corresponding pages
    When I open the "Payments" quick-link tile
    Then the Payments page should be displayed

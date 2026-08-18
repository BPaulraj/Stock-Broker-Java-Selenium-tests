Feature: Registration

  Background:
    Given I open the StockBroker application
    And I go to the sign up page

  Scenario: Successful registration logs the new user straight into the dashboard
    When I register with a new, unique account
    Then I should be logged in

  Scenario: The "Log in" link on the sign up page returns to the login form
    When I follow the log in link
    Then the login page should be displayed

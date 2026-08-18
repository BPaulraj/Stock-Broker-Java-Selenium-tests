Feature: Login

  Background:
    Given I open the StockBroker application

  Scenario: Successful login with valid credentials
    When I log in with a valid email and password
    Then I should be logged in

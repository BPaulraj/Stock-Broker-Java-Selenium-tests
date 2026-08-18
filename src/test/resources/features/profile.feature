Feature: Profile

  Background:
    Given I open the StockBroker application
    When I log in with a valid email and password
    Then I should be logged in
    And I go to the profile page

  Scenario: Account summary matches the logged-in user
    Then the account email should match the logged-in user's email

  Scenario: Saving profile changes shows a success message
    When I update the phone number to "9998887777"
    And I save the profile changes
    Then the profile status message should confirm the update

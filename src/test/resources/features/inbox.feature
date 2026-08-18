Feature: Inbox

  Background:
    Given I open the StockBroker application
    When I log in with a valid email and password
    Then I should be logged in
    And I go to the inbox page

  Scenario: Selecting a message shows its details
    When I select the first message in the inbox
    Then the message detail should show a non-empty subject and body

  Scenario: Toggling the read state flips the mark-read button label
    Given I select the first message in the inbox
    When I toggle the message's read state
    Then the mark-read button label should have changed

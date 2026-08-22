@api
Feature: REST API - Inbox
  Covers GET /inbox and GET /inbox/:id on the versioned REST API. Registration
  triggers a "Welcome to StockBroker Demo" SYSTEM message, so a freshly
  registered account always has at least one message to read.

  Background:
    Given I have a fresh registered account via the API

  Scenario: Registration seeds a welcome message in the inbox
    When I list my inbox messages via the API
    Then the API response status code should be 200
    And the API response field "[0].type" should equal "SYSTEM"
    And the API response field "[0].subject" should equal "Welcome to StockBroker Demo"

  Scenario: Fetching an inbox message by id returns its detail
    Given I note the first inbox message id via the API
    When I fetch the noted inbox message via the API
    Then the API response status code should be 200
    And the API response field "subject" should equal "Welcome to StockBroker Demo"

  Scenario: Fetching a non-existent inbox message returns 404
    When I fetch inbox message "does-not-exist" via the API
    Then the API response status code should be 404

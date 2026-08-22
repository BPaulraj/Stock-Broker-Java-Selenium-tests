@api
Feature: REST API - Authentication and profile
  Covers POST /users (register), POST /sessions (login), GET/PUT /users/me
  on the versioned REST API (http://localhost:4100/api/v1).

  Scenario: Registering a new account returns an access token and user profile
    Given I have a fresh registered account via the API
    Then the API response status code should be 201
    And the API response field "accessToken" should not be null
    And the API response field "user.kycStatus" should equal "UNVERIFIED"

  Scenario: Registering with an email that is already in use is rejected
    Given I have a fresh registered account via the API
    When I register via the API with my existing account's email
    Then the API response status code should be 409
    And the API error message should contain "already exists"

  Scenario: Registering with a weak password is rejected
    When I register a new account via the API with email "weak-pw-test@example.com" and password "short1"
    Then the API response status code should be 400
    And the API response field "password" should have a validation error mentioning "at least 8 characters"

  Scenario: Logging in with valid credentials returns a fresh access token
    Given I have a fresh registered account via the API
    When I log in via the API with my registered account's credentials
    Then the API response status code should be 201
    And the API response field "accessToken" should not be null

  Scenario: Logging in with an incorrect password is rejected
    Given I have a fresh registered account via the API
    When I log in via the API with email "someone-not-registered@example.com" and password "WrongPass123!"
    Then the API response status code should be 401
    And the API error message should contain "Invalid email or password"

  Scenario: Fetching the current user without a token is rejected
    When I request my profile via the API without an access token
    Then the API response status code should be 401

  Scenario: Fetching the current user with an invalid token is rejected
    When I request my profile via the API with an invalid access token
    Then the API response status code should be 401

  Scenario: Fetching the current user returns the authenticated user's profile
    Given I have a fresh registered account via the API
    When I request my profile via the API
    Then the API response status code should be 200
    And the API response field "email" should not be null

  Scenario: Updating the profile persists the new name
    Given I have a fresh registered account via the API
    When I update my profile via the API with name "Claude Updated Name"
    Then the API response status code should be 200
    And the API response field "name" should equal "Claude Updated Name"

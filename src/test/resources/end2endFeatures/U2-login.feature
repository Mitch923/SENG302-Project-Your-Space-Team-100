Feature: As Sarah, I want to log into the system so that I can have a personalised experience with it and enjoy its features.

  Scenario: blue sky - Given I am on the login form, and I enter an email address and its corresponding password for an
  account that exists on the system, when I click the “Sign In” button, then I am taken to the main page of
  the application.
    Given I am on the login page
    When I enter the email "john@example.com" and password "P4$$word"
    And I click the sign in button
    Then I am redirected to the main page


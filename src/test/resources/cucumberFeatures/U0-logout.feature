Feature: U0 As Sarah, I want to be able to logout of my account once I am finished my session.

  Background:
    Given There is a user with details "Sarah", "Thompson", "sarahandjackthompson@gmail.com", "P4$$word" who is logged in

  @U0_AC2
  Scenario: U0 AC2 - Given that I was logged in to the app, when I click the
  logout button, I can no longer access feature that require login.
    When I click logout
    And I try access a page that requires logging in
    Then I am redirected to the login page

  @U0_AC4
  Scenario: U0 AC4 - Given that I was logged in to the app, when I click the logout button,
  I am redirected to the landing page.
    When I click logout
    Then I am redirected to the landing page

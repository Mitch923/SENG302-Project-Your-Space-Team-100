Feature: U10019 - As Inaya I want to be able vote on designs listed in the weekly competition so that I can express which designs I think are the best.

  Background:
    Given The system contains the default users, renovations, and competitions
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U10019_AC5
  Scenario: AC5 - Given I am on the home page, when I navigate to the competition details page, then the 1st, 2nd, and 3rd place designs are displayed prominently at the top of the competition page.
    Given I am on the Home page
    When I navigate to the competition details page
    Then the 1st, 2nd, and 3rd place designs are displayed prominently at the top of the competition page

  @U10019_AC6
  Scenario: AC6 - Given I am logged in, when I go to the home page, then I can see the current weekly competition and its current top 3 entries.
    Given I am logged in
    When I go to the home page
    Then I can see the current weekly competition and its current top 3 entries
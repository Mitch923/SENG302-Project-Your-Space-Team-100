Feature: As Sarah, I want to edit my user profile so that I can keep my details accurate.

  Scenario: Blue sky scenario - Given I am on the edit profile form, and I enter valid values for my first name, last name, and email
  address, when I click the “Submit” button, then my new details are saved, and I am taken back to my
  profile page.
    Given I am editing my user profile
    And I enter valid values for first name, last name, and email
    When I submit my changes
    Then I am taken back to my profile page
    And My details have been updated

Feature: U10013 As Inaya, I want to view details of a current competition so that I can see more information about the competition and view the design entries.

  Background:
    Given The system contains the default data
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U10013_AC1
  Scenario: U10013 AC1 - Given I am logged into the system and on the home page, when I click the
  ‘View All’  button for the currently open competition, then I am taken to the competition details
  page where I see the competitions name, total number of designs entered, start date end date and
  time and a list of all designs that have been entered in the competition.

    Given I am on the home page
    When I click the view all button for the currently open competition
    Then I am taken to the competition details page
    And I can see the first page of designs
Feature: U10022 - As Inaya, when I am viewing a current or past competitions details, I want to be able to sort the designs entered in the competition so that, I better find the designs I want to see.

  Background:
    Given The system contains the default users, renovations, and competitions
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U10022_AC2
  Scenario: AC2 - Given I am on the competition details page, and the competition has designs entered in it, when I select "Most Votes" from the sorting options, then the designs are displayed in descending order by vote count with the highest votes first.
    Given I am on the competition details page for the current competition
    When I select the "Most Votes" option from the sort by dropdown
    Then the designs are displayed in descending order by vote count with the highest voted design shown first

  @U10022_AC3
  Scenario: AC3 - Given a competition has designs entered in it, when I select "Least Votes" from the sorting options, then the designs are displayed in ascending order by vote count with the lowest votes first.
    Given I am on the competition details page for the current competition
    When I select the "Least Votes" option from the sort by dropdown
    Then the designs are displayed in ascending order by vote count with the lowest voted design shown first

  @U10022_AC4
  Scenario: AC4 - Given I am on the competition details page, and the competition has designs entered in it, when I select "Alphabetical (A-Z)" from the sorting options, then the designs are displayed in ascending alphabetical order by design name using case-insensitive sorting.
    Given I am on the competition details page for the current competition
    When I select the "Alphabetical (A-Z)" option from the sort by dropdown
    Then the designs are displayed in ascending alphabetical order by design name

  @U10022_AC5
  Scenario: AC5 - Given I am on the competition details page, and the competition has designs entered in it, when I select "Alphabetical (Z-A)" from the sorting options, then the designs are displayed in descending alphabetical order by design name using case-insensitive sorting.
    Given I am on the competition details page for the current competition
    When I select the "Alphabetical (Z-A)" option from the sort by dropdown
    Then the designs are displayed in descending alphabetical order by design name

  @U10022_AC6
  Scenario: AC6 - Given I am viewing a competition's design list for the first time, when the page loads, then the designs are displayed in the default sort order of "Most Votes" and the sorting dropdown indicates the current sort option.
    Given I am on the home page
    When go to the competition details page for the current competition
    Then by default the designs are displayed in descending order by vote count with the highest voted design shown first

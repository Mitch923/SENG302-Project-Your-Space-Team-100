Feature: U11 As Kaia, I want to edit information about my renovation record so I can keep it up to date.

  Background:
    Given There is a user with details "Sarah", "Thompson", "sarahandjackthompson@gmail.com", "P4$$word" who is logged in

  @U11_AC1
  Scenario: U11 AC1 -Given I am on a renovation record details page for a renovation record I own, when I click the “Edit”
  button, then I see the edit renovation record form with all the details prepopulated.
    Given I am on the renovation record details page
    When I click edit
    Then I am on the edit renovation form with all the details prepopulated

  @U11_AC2
  Scenario: U11 AC2 - Given I am on the edit renovation record form, and I enter valid values for the name, description,
  and optionally rooms, when I click “Submit”, then the renovation record details are updated, and I am
  taken back to the renovation record page.
    Given I am on the edit renovation form
    And I enter valid values for the name description and a room
    When I click submit
    Then The renovation record details are updated and I am taken to the renovation record page

  @U11_AC3
  Scenario: U11 AC3 - Given I am on the edit renovation record form, and I enter an empty, blank, or invalid (i.e. nonalphanumeric
  characters other than spaces, dots, commas, dot, hyphens, or apostrophes) record name,
  when I click “Submit”, then an error message tells me “Renovation record name cannot by empty” or
  “Renovation record name must only include letters, numbers, spaces, dots, hyphens or apostrophes”, and
  the record is not updated.
    Given I am on the edit renovation form
    And I enter a blank record name
    When I click submit
    Then Then an error message tells me the name cannot be empty

  @U11_AC3.1
  Scenario: U11 AC3.1 - Given I am on the edit renovation record form, and I enter an empty, blank, or invalid (i.e. nonalphanumeric
  characters other than spaces, dots, commas, dot, hyphens, or apostrophes) record name,
  when I click “Submit”, then an error message tells me “Renovation record name cannot by empty” or
  “Renovation record name must only include letters, numbers, spaces, dots, hyphens or apostrophes”, and
  the record is not updated.
    Given I am on the edit renovation form
    And I enter a invalid renovation name
    When I click submit
    Then Then an error message tells me the categories of characters that are allowed and renovation is not updated

  @U11_AC4
  Scenario: U11 AC4 - Given I am on the edit renovation record form, and I enter a record name that is not unique across
  all my renovation record, when I click “Submit”, then an error message tells me that the name is not
  unique, and the record is not updated.
    Given I am on the edit renovation form
    And I enter a non-unique renovation name
    When I click submit
    Then Then an error message tells me the name is not unique

  @U11_AC8
  Scenario: U11 AC8 - Given I am on the edit renovation record form, and enter a description longer than 512 characters,
  when I click “Submit”, then an error message tells me “Renovation record description must be 512
  characters or less”, and the record is not updated.
    Given I am on the edit renovation form
    And I enter a description longer than 512 characters
    When I click submit
    Then Then an error message tells me the the description must be 512 characters or less

  @U11_AC10
  Scenario: U11 AC10 - Given I am on the edit renovation record form, when I click “Cancel”, then I am taken back to the
  renovation record details page, and no fields have been updated.
    Given I am on the edit renovation form
    When I click cancel on the edit renovation page
    Then Then I am taken back to the renovation record details page and the changes were not saved
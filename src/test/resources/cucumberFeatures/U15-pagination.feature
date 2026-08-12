Feature: U15 As Kaia, I want to view my designs conveniently so that I don't have to scroll endlessly to find my design.

  Background:
    Given The system contains the default data
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U15_AC1
  Scenario: U15 AC1
    Given there are more designs in my renovation record that my screen size can handle
    When I see the list of records
    Then the list is divided into sub-lists with pagination number

  @U15_AC2
  Scenario Outline: U15 AC2
    Given I see the list of designs, and pagination numbers
    When I click on a <page number>
    Then I see the list of designs corresponding to that <page number>
    Examples:
      | page number |
      | 1           |
      | 2           |
      | 3           |
      | 4           |

  @U15_AC5
  Scenario Outline: U15 AC5
    Given I see the list of designs, and pagination numbers, and there are more than 10 pages
    When I input a <page number> within the range of available pages
    And I confirm that I want to go to that page
    Then I see the list of designs corresponding to that <page number>
    Examples:
      | page number |
      | 1           |
      | 2           |
      | 3           |
      | 4           |
      | 5           |
      | 6           |
      | 7           |
      | 8           |
      | 9           |
      | 10          |
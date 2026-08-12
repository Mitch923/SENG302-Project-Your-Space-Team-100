Feature: U17 - As Kaia, I want to be able to search within my renovation records so that I can find
  renovation records that I have carried in one of my rentals that I can reuse for later.

  Background:
    Given The system contains the default data
    And I am logged into the account with email "john@example.com" and password "P4$$word"
    And Session is created

  @U17_AC2
  Scenario Outline: U17 AC2 - Given I enter a search string in the search bar, when I click a search button either labelled “search”
  or with a magnifying glass icon, then I am shown only my renovation records whose name or description
  include my search value.
    Given I enter a search <string> in the search bar
    When I submit my search
    Then I am shown only my renovation records
    And I am shown only renovation records whose name or description include my search values
    Examples:
      | string     |
      | ""         |
      | "Record"   |
      | "Hey"      |
      | "Bathroom" |
      | "bathroom" |

  @U17_AC4
  Scenario Outline: U17 AC4 - Given I enter a search string that has no matches, when I click a search button
  either labelled “search” or with a magnifying glass icon, then a message tells me “No renovations
  match your search”.
    Given I enter a search <string> that has no matches
    When I submit my search
    Then There are no results
    Examples:
      | string |
      | "abc"  |
      | "1234" |
      | "xyz"  |
      | "q"    |

  @U17_AC5
  Scenario: U17 AC5 - Given I have run a search, when there are more records than the screen can handle, then
  I see pagination buttons, and the results are split into pages.
    Given I enter a search "" in the search bar
    And There are more records than the screen can handle
    When I submit my search
    Then The results are split into pages

  @U17_AC6
  Scenario Outline: U17 AC6 - Given I see the list of records, and pagination numbers, when I click on a page number,
  then I see the list of renovation records corresponding to that page number, and the page I am
  currently in is highlighted.
    Given I see the list of records, and pagination numbers with search ""
    When I click on a page number <page number>
    Then I see the list of renovation records corresponding to that page number <page number>
    Examples:
      | page number |
      | 1           |
      | 2           |
      | 3           |
      | 5           |
      | 6           |
      | 7           |
      | 8           |
      | 9           |
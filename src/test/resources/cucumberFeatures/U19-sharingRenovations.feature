Feature: As Inaya, I want to be able to make my renovation record public so that I can share my progress with other.

  Background:
    Given The system contains the default data
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U19_AC1
  Scenario: U19 AC1 - Given I am on the renovation record details page for a record I own, when I toggle on a
  toggle labelled “Public”, then my renovation record will be visible in search results for all
  logged in users.
    Given I am on the renovation record details page for a record I own
    And The record is not marked as public
    When I toggle on a toggle labelled 'Public'
    Then My renovation record will be visible in search results for all logged in users

  @U19_AC2
  Scenario: U19 AC2 - Given I am on the renovation record details page for a record I own and have already
  marked as public, when I toggle off the toggle labelled “Public”, then my renovation record will
  no longer be visible in search results from other users, but it will remain visible in my search
  results.
    Given I am on the renovation record details page for a record I own
    And The record is not marked as public
    When I toggle off the toggle labelled 'Public'
    Then It will remain visible in my search results
    And My renovation record will no longer be visible in search results from other users

  @U19_AC3
  Scenario: U19 AC3 - Given I am viewing the list of public renovation records, when there are too many
  records to be shown on a single page, then the list of records is divided in sub-lists with
  pagination numbers, and the pagination buttons include “Prev” and “Next” buttons for easy
  navigation.
    Given I am viewing the list of public renovation records
    When There are too many records to be shown on a single page
    Then The list of records is divided in sub-lists with pagination numbers

  @U19_AC4
  Scenario: U19 AC4 - Given I am anywhere on the system, when I click a “Browse renovations” button, then I
  see a list of public renovation records sorted by more recently created ones in descending order.
    When I click the 'Browse renovations' button
    Then I see a list of public renovation records
    And The records are sorted by more recently created ones in descending order

  @U19_AC7
  Scenario Outline: U19 AC7 - Given I see the list of public renovation records, and pagination numbers, and there are
  more than 10 pages, when I input a page number within the range of available pages, and I
  confirm that I want to go to that page, then I see the list of renovation records corresponding
  to that page.
    Given I see the list of public renovation records, and pagination numbers
    And There are more than 10 pages of public renovation records
    When I input a <page number> within the range of available pages of public renovation records
    And I confirm that I want to go to that page number of public renovation records
    Then I see the list of public renovation records corresponding to that <page number>
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
      | 11          |

  @U19_AC9
  Scenario Outline: U19 AC9 - Given I see the list of public renovation records, when I click on a renovation record,
  then I see the details of that renovation record.
    Given I see the list of public renovation records
    When I click on a renovation record with name <name>
    Then I see the details of that renovation record with name <name>
    Examples:
      | name                   |
      | "Renovation Record 1"  |
      | "Renovation Record 3"  |
      | "Renovation Record 6"  |
      | "Renovation Record 7"  |
      | "Renovation Record 54" |


#  @U19_AC10
#  Scenario: U19 AC10 - Given I see a renovation record I found from a search, when I click a “Back to search
#    results” button, then I see the list of renovation records at the exact same page I was before
#    clicking on that renovation record to see its details.
#    Given I see a renovation record I found from a search
#    When I click a 'Back to search results' button
#    Then I see the list of renovation records at the exact same page I was before clicking on that renovation record
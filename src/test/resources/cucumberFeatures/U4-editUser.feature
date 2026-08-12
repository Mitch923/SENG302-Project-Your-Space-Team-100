Feature: U4 As Sarah, I want to edit my user profile so that I can keep my details accurate

  Background:
    Given The system contains the default data
    Given There is a user with details "Sarah", "Thompson", "sarah@example.com", "P4$$word" who is logged in

  @U4_AC1
  Scenario: U4 AC1 - Given I am on my user profile page, when I click the “Edit” button, then I see the edit profile form
  with all my details prepopulated except my password.
    Given I am on my user profile page
    When I click the Edit button
    Then I see the edit profile form

  @U4_AC2
  Scenario Outline: U4 AC2 - Given I am on the edit profile form, and I enter valid values for my first name, last name, and email
  address, when I click the “Submit” button, then my new details are saved, and I am taken back to my
  profile page.
    Given I am on the edit profile form
    When I submit my edit profile changes with values <first_name>, <last_name>, "sarah@example.com"
    Then My new details are saved
    And I am taken back to the profile page
    Examples:
      | first_name  | last_name |
      | "Jane"      | "Doe"     |
      | "John-Snow" | "Smith."  |

  @U4_AC3
  Scenario Outline: U4 AC3 - Given I am on the edit profile form, and I enter invalid values (i.e. an empty or blank
  first name, nonalphabetical characters except hyphen, space or apostrophe for either first
  or last name), when I click the “Submit” button, then an error message tells me “First name
  cannot by empty” or “{First/Last} name must only include letters, spaces, hyphens or apostrophes”,
  and no changes are saved.
    Given I am on the edit profile form
    When I submit my edit profile changes with values <first_name>, <last_name>, "sarah@example.com"
    Then An error message tells me: <error_message>
    And No changes are saved
    Examples:
      | first_name | last_name | error_message                                                          |
      | ""         | "Doe"     | "First name cannot be empty"                                           |
      | "@@@"      | "Doe"     | "First name must only include letters, spaces, hyphens or apostrophes" |
      | "Jane"     | "###"     | "Last name must only include letters, spaces, hyphens or apostrophes"  |

  @U4_AC4
  Scenario Outline: U4 AC4 - Given I am on the edit profile form, and I enter a first or last name that is more than 64 characters,
  when I click the “Submit” button, then an error message tells me “{First/Last} name must be 64 characters
  long or less”, and no changes are saved.
    Given I am on the edit profile form
    And I submit my edit profile changes with values <first_name>, <last_name>, "sarah@example.com"
    Then An error message tells me: <error_message>
    And No changes are saved
    Examples:
      | first_name                                                          | last_name                                                           | error_message                                   |
      | "JaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJane1" | "Doe"                                                               | "First name must be 64 characters long or less" |
      | "John-Snow"                                                         | "SmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmith" | "Last name must be 64 characters long or less"  |

  @U4_AC5
  Scenario Outline: U4 AC5 - Given I am on the edit profile form, and I enter an empty or malformed email address (i.e. an email
  address that does not have a prefix, second-level domain, and top-level domain or has invalid characters),
  when I click the “Submit” button, then an error message tells me “Email address must be in the form
  ‘jane@doe.nz’”, and no changes are saved.
    Given I am on the edit profile form
    When I submit my edit profile changes with values "Sarah", "Thompson", <email>
    Then An error message tells me: <error_message>
    And My email is not updated
    Examples:
      | email                    | error_message                                     |
      | "sarah..@example.com"    | "Email address must be in the form 'jane@doe.nz'" |
      | "@example.com"           | "Email address must be in the form 'jane@doe.nz'" |
      | "sarah.thompson@example" | "Email address must be in the form 'jane@doe.nz'" |

  @U4_AC6
  Scenario: U4 AC6 - Given I am on the edit profile form, and I enter an email address associated to an account that already
  exists, when I click the “Submit” button, then an error message tells me “This email address is already in
  use”, and no changes are saved.
    Given I am on the edit profile form
    When I submit my edit profile changes with values "Janet", "Jackson", "john@example.com"
    Then An error message tells me: "This email address is already in use"
    And No changes are saved

  @U4_AC7
  Scenario: U4 AC7 - Given I am on the edit profile form, when I click the “Cancel” button, I am taken back to my profile
  page, and no changes have been made to my profile.
    Given I am on the edit profile form
    When I click the Cancel button
    Then I see the profile page
    And No changes are saved
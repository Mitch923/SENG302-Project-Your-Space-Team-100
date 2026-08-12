Feature: As Inaya, I want to be able to tag my renovation records with common tags so that my
  renovation records are more easily browsable by others interested in those tags.

  Background:
    Given The system contains the default data
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U20_AC2
  Scenario Outline: AC2 - Given I want to add a tag, when I input tags made of only special characters or
  numbers, then I see an error message telling me that “tags must contain letters”, and the tag is
  not added to the renovation record.
    Given I want to add a tag to my renovation record
    When I input a tag <tag-name> of only special characters or numbers
    Then I see an error message telling me that "tags must contain letters"
    And The tag <tag-name> is not added to the renovation record
    Examples:
      | tag-name |
      | "123"    |
      | "!@#"    |
      | "1(400@" |
      | "(12"    |
      | ")%(#@*" |
      | "123456" |

  @U20_AC5
  Scenario: AC5 - Given I want to add a tag to my renovation record, when there are five tags
  associated to that renovation record, then I cannot add another tag.
    Given I want to add a tag to my renovation record
    And There are already five tags associated to that renovation record
    When I try to add a tag to the renovation record
    Then The tag is not added

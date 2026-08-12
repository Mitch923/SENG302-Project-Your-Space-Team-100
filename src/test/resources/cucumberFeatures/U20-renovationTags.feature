Feature: U20 As Inaya, I want to be able to tag my renovation records with common tags so that my renovation records are more easily browsable by others interested in those tags.

  Background:
    Given There is a user with details "Sarah", "Thompson", "sarahandjackthompson@gmail.com", "P4$$word" who is logged in
    And There is a tag "Bathroom" in the database
    And There is a tag "Balcony" in the database
    And There is a tag "EPICNESS" in the database

  @U20_AC1
  Scenario: AC1 - Given I am on the renovation record details page for a record I own, when I start typing in the tag
  field, then there is auto completion showing existing tags that match my input.

    Given I am on the renovation record details page
    When I type "B" in the tag field
    Then there is auto completion showing existing tags that match my input "B".

  @U20_AC7
  Scenario Outline: AC2 - Given I want to add a tag, when I input tags made of only special characters or numbers, then I see
  an error message telling me that “tags must contain letters”, and the tag is not added to the renovation
  record.

    Given I am on the renovation record details page
    When when I use mix cased words <mix_cased_words>
    Then there is auto completion showing existing tags that match my input <mix_cased_words>.

    Examples:
      | mix_cased_words |
      | "baLcOny"       |
      | "bAlCONY"       |
      | "bA"            |
      | "Ba"            |
      | "ePIc"          |
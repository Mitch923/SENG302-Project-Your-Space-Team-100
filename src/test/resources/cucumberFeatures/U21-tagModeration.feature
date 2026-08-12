Feature: U21 As Inaya, I want to be able to know when I input an inappropriate tag so that the system remains free from inappropriate language.

  Background:
    Given There is a user with details "Sarah", "Thompson", "sarahandjackthompson@gmail.com", "P4$$word" who is logged in

  @U21_AC1 @U21_AC2
  Scenario Outline: AC1 - Given I type a tag for a renovation record, when I try to input an inappropriate word (e.g.,
  swearing), then my tag is not added to the renovation record, and I receive an error message telling me
  that my tag is not following the system language standards.
  AC2 - Given I type a tag for a renovation record, when I try to input an inappropriate word, and I try to
  use slight variations to hide the actual word (e.g., sh*t), then my tag is not added to the renovation
  record, and I receive an error message telling me that my tag is not following the system language
  standards.

    When I type a profane word "<tagInput>" in the tag field and click Add Tag
    Then I receive an error message that tells me the tag doesn't meet the system language standards
    Examples:
      | tagInput |
      | shit     |
      | fUCk     |
      | sh&t     |
      | s.h.i.t  |
      | s#it     |
      | fu&k     |
      | fu&ker   |
      | bitch    |



Feature: U1 As Sarah, I want to register on Your Space so that I can use its awesome features.

  @U1_AC2
  Scenario: U1 AC2 - Given I am on the registration form, and I enter valid values for my first name,
  email address, type the same password twice, when I click the “Sign Up” button, then I am
  automatically logged in to my new account, and I see my user profile page.
    Given I enter valid first name email address and password values
    When I click sign up
    Then I am redirected to the verification page

  @U1_AC3
  Scenario Outline: U1 AC3 - Given I am on the registration form, and I enter invalid name values,
  when I click the “Sign Up” button, then an error message tells me “First name cannot be
  empty” or “{First/Last} name must only include letters, spaces, hyphens or apostrophes”,
  and no account is created.
    Given I enter <first name> and <last name>
    When I click sign up
    Then an error message tells me <error message>
    Examples:
      | first name | last name | error message                                                          |
      | ""         | ""        | "First name cannot be empty"                                           |
      | ""         | "Davis"   | "First name cannot be empty"                                           |
      | "S4m"      | ""        | "First name must only include letters, spaces, hyphens or apostrophes" |
      | "S@m"      | ""        | "First name must only include letters, spaces, hyphens or apostrophes" |
      | "Sam"      | "D4vis"   | "Last name must only include letters, spaces, hyphens or apostrophes"  |
      | "Sam"      | "D@vis"   | "Last name must only include letters, spaces, hyphens or apostrophes"  |

  @U1_AC3.1
  Scenario Outline: U1 AC3.1 - Given I am on the registration form, and I enter invalid name values,
  when I click the “Sign Up” button, then an error message tells me “First name cannot be
  empty” or “{First/Last} name must only include letters, spaces, hyphens or apostrophes”,
  and no account is created.
    Given I enter <first name> and <last name>
    When I click sign up
    Then an error message tells me about both first and last name includes errors
    Examples:
      | first name | last name |
      | "S4m"      | "D4vis"   |
      | "S@m"      | "D4vis"   |
      | "Sam2&"    | "Davis*^" |

  @U1_AC3.2
  Scenario Outline: U1 AC3.2 - Given I am on the registration form, and I enter invalid name values,
  when I click the “Sign Up” button, then an error message tells me “First name cannot be
  empty” or “{First/Last} name must only include letters, spaces, hyphens or apostrophes”,
  and no account is created.
    Given I enter <first name> and <last name>
    When I click sign up
    Then an error message tells me first name cannot be empty and last name must only include
    Examples:
      | first name | last name    |
      | ""         | "Sam()Davis" |
      | ""         | "Davis_back" |
      | ""         | "Davis*^"    |

  @U1_AC4
  Scenario Outline: U1 AC4 - Given I am on the registration form, and I enter a first or last name
  that is more than 64 characters, when I click the “Sign Up” button, then an error message tells
  me “{First/Last} name must be 64 characters long or less”.
    Given I enter <first name> and <last name>
    When I click sign up
    Then an error message tells me first or last name must be 64 characters long or less
    Examples:
      | first name                                                          | last name                                                           |
      | "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbb" | ""                                                                  |
      | "Jasper"                                                            | "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbb" |

  @U1_AC4.1
  Scenario Outline: U1 AC4.1 - Given I am on the registration form, and I enter a first or last name
  that is more than 64 characters, when I click the “Sign Up” button, then an error message tells
  me “{First/Last} name must be 64 characters long or less”.
    Given I enter <first name> and <last name>
    When I click sign up
    Then an error message tells me both first and last name are too long
    Examples:
      | first name                                                          | last name                                                           |
      | "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbb" | "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbb" |

  @U1_AC5
  Scenario Outline: U1 AC5 - Given I am on the registration form, and I enter an empty or malformed1 email address (i.e. an email
  address that does not have a prefix, second-level domain, and top-level domain or has invalid characters),
  when I click the “Sign Up” button, then an error message tells me “Email address must be in the form ‘jane@doe.nz’”.
    Given I enter invalid email "<email>"
    When I click sign up
    Then an error message tells me Email address must be in the form jane@doe.nz
    Examples:
      | email              |
      | jane@doe.          |
      | janedoe.nz         |
      | @gmail.com         |
      | jane;doe@gmail.com |

  @U1_AC6
  Scenario: U1 AC6 - Given I am on the registration form, and I enter an email address associated to an account that
  already exists, when I click the “Sign up” button, then an error message tells me “This email address is
  already in use”.
    Given I enter "jane@doe.nz" for my email
    And "jane@doe.nz" has already been used to register
    When I click sign up
    Then an error message tells me This email is already in use

  @U1_AC7
  Scenario: U1 AC7 -Given I am on the registration form, and I enter two different passwords, when I click the “Sign Up”
  button, then an error message tells me “Passwords do not match”.
    Given I enter "P4$$word" for my password
    And I enter "password" to confirm my password
    When I click sign up
    Then an error message tells me passwords do not match

  @U1_AC8
  Scenario Outline: U1 AC8 - Given I am on the registration form, and I enter a weak password (i.e. is less than 8 characters, does
  not contain at least one lower case letter, one upper case letter, one number, and one special character),
  when I click the “Sign Up” button, then an error message tells me “Your password must be at least 8
  characters long and include at least one uppercase letter, one lowercase letter, one number, and one
  special character.”
    Given I enter invalid password "<password>"
    When I click sign up
    Then an error message tells me my password doesn't meet the requirements
    Examples:
      | password |
      | Pa$$word |
      | short    |
      | P4ssword |
      | p4$$word |

  @U1_AC9
  Scenario: U1 AC9 - Given I am on the registration form, when I click the “Cancel” button, then I am taken back to the
  system’s home page.
    Given I am on the registration form
    When I click cancel
    Then I am redirected to the home page

  @U1_AC10
  Scenario: U1 AC10 - Given I have created an account, when my password is saved, then it is encrypted in a nonrecoverable
  way.
    Given I enter valid values to sign up with password "P4$$word"
    When I click sign up
    Then The password saved doesn't exactly match the password entered
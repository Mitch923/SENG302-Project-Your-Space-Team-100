Feature: U6 - As Sarah, I want to confirm my account by email when I register so that may account
  is more secure.As Sarah, I want to confirm my account by email when I register so that may
  account is more secure.As Sarah, I want to confirm my account by email when I register
  so that may account is more secure.

  @U6_AC1
  Scenario: U6 AC1 - Given I submit a fully valid registration form, when I click the
  Register button, then a confirmation email is sent to my email address, and a unique
  signup code is included in the email, and the email contains a message “if you didn’t register,
  ignore this email, and your account will be deleted in 10 minutes.”, and I’m presented with a
  page asking for the signup code.

    Given I register to Your Space with valid registration details
    When I click the Register button
    Then A unique signup code is generated for the email
    And A confirmation email is sent to my email address
    And I am sent to a page asking for the signup code

  @U6_AC2
  Scenario: U6 AC2 - Given a signup code has been created for a new user, when 10 minutes have
  passed after the signup code was sent, then the code and account are deleted.

    Given I register to Your Space with valid registration details
    And I click the Register button
    When A unique signup code is generated for the email
    And I wait for 10 minutes
    Then The account is deleted
    And The code is deleted


  @U6_AC3
  Scenario: U6 AC3 - Given I received a signup code, and the code has expired, when I try to
  use the signup code, then an error message “Signup code invalid” is displayed.

    Given I register to Your Space with valid registration details
    And I click the Register button
    And A unique signup code is generated for the email
    And I wait for 10 minutes
    When I submit my code
    Then An error message: Signup code invalid, is displayed

  @U6_AC4
  Scenario: U6 AC4 - Given I received a signup code, and I have not confirmed my registration
  yet, and my code has not expired, when I want to log in to the system for the first time, then I
  must use the signup code.

    Given I register to Your Space with valid registration details
    And I click the Register button
    When I log in to the system
    Then I am sent to a page asking for the signup code

  @U6_AC5
  Scenario: U6 AC5 - Given I received a signup code over email, and I navigate to the signup
  code page, when I enter the  signup code linked to my account, then the system validates the code
  successfully, and I am redirected to the login page that tells me “Your account has been
  activated, please log in”.

    Given I register to Your Space with valid registration details
    And I click the Register button
    And A unique signup code is generated for the email
    When I submit my code
    Then The system validates my code successfully
    And I get redirected to the login page

  @U6_AC6
  Scenario: U6 AC6 - Given I am on the signup code page, when I enter an invalid/unknown
  signup code linked to my account, then an error message “Signup code invalid” is displayed,
  and my registration is not confirmed.
    Given I register to Your Space with valid registration details
    And I click the Register button
    And A unique signup code is generated for the email
    When I submit an unknown code
    Then An error message: Signup code invalid, is displayed
    And My account is not verified
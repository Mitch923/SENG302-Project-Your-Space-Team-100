Feature: As Sarah, I want to log into the system so that I can have a personalised experience with it and enjoy its features.

  Background:
    Given There is no user logged in
    Given I am on the login form

  @U2_AC2
  Scenario: U2 AC2 - Given I am on the login form, and I enter an email address and its corresponding password for an
  account that exists on the system, when I click the “Sign In” button, then I am taken to the main page of
  the application.
    Given I enter the email "john@example.com" and password "P4$$word"
    When I click the sign in button
    Then I am redirected to the main page

  @U2_AC3
  Scenario: U2 AC3 - Given I am on the login form, when I click a highlighted link with the text “Not registered? Create an
  account”, then I am taken to the registration page.
    When I click the not registered link
    Then I am redirected to the registration page


  @U2_AC5
  Scenario Outline: U2 AC5 - Given I am on the login form, and I enter an email address that is unknown to the system, when I
  click the “Sign In” button, then an error message tells me “The email address is unknown, or the password
  is invalid”.
    Given I enter the email <email>
    When I click the sign in button
    Then the login error message appears

    Examples:
      | email              |
      | "jane@example.com" |
      | "test@gmail.com"   |

  @U2_AC6
  Scenario Outline: U2 AC6 - Given I am on the login form, and I enter an empty password or the wrong password for the
  corresponding email address, when I click the “Sign In” button, then an error message tells me “The email
  address is unknown, or the password is invalid”.
    Given I enter the email <email>
    And I enter the password <password>
    When I click the sign in button
    Then the login error message appears

    Examples:
      | email              | password  |
      | "jane@example.com" | ""        |
      | "john@example.com" | "test123" |
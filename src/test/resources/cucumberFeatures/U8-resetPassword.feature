Feature: U8 As Sarah, I want to be able to change my password over email,
  so that I can still access my account even if I forget my password

  Background:
    Given There is a user with details "Sarah", "Thompson", "sarahandjackthompson@gmail.com", "P4$$word" who is not logged in.

  @U8_AC3
  Scenario: Given I am on the lost password form, and I enter a valid email that is not known to the system, when I click “Submit”, then a confirmation message tells me “An email was sent to the address if it was recognised”.
    Given I am on the lost password form
    And I enter a valid email that is not known to the system
    When I click the Submit button
    Then a confirmation message tells me "An email was sent to the address if it was recognised"


  @U8_AC4
  Scenario:  Given I am on the lost password form, and I enter an email that is known to the system, when I click “Submit”, then a confirmation message tells me “An email was sent to the address if it was recognised”, and an email is sent to the email address with a link containing a unique reset token to update the password of the profile associated to that email.
    Given I am on the lost password form
    And I enter an email that is known to the system
    When I click the Submit button
    Then a confirmation message tells me "An email was sent to the address if it was recognised"
    And an email is sent to the email address with a link containing a unique reset token to update the password of the profile associated to that email.


  @U8_AC5
  Scenario: Given I received an email to reset my password, when I go to the given URL passed in the email, then I am asked to supply a new password with “new password” and “retype password” fields.
    Given I received an email to reset my password
    When I go to the given URL passed in the email
    Then I am asked to supply a new password with “new password” and “retype password” fields.

  @U8_AC6
  Scenario: Given I am on the reset password form, and I enter two different passwords in “new password” and “retype password” fields, when I hit the save button, then an error message tells me “The passwords do not match”, and the password is not updated.
    Given I am on the reset password form
    And I enter two different passwords in “new password” and “retype password” fields
    When I hit the save button
    Then an error message on the model says "The passwords do not match"
    And the password is not updated

  @U8_AC7
  Scenario: Given I am on the reset password form, and I enter a weak password (e.g., contains any other fields from the user profile form, is below 8 char long, does not contain at least one uppercase letter, one lowercase letter, one number, and one special character), when I hit the “Submit” button, then an error message tells “Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.”, and the password is not updated.
    Given I am on the reset password form
    And I enter a weak password
    When I hit the save button
    Then an error message on the model says "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
    And  the password is not updated

  @U8_AC8
  Scenario: Given I am on the reset password form, when I enter fully compliant details and click “Submit”,
  then my password is updated, and an email is sent to my email address to confirm that my password has been updated, and I am redirected to the login page
    Given I am on the reset password form
    When I enter fully compliant details
    And I hit the save button
    Then my password is updated
    And an email is sent to my email address to confirm that my password has been updated
    And I am taken to the login page

  @U8_AC9
  Scenario: Given I am on the reset password form, and 10 minutes have passed since the reset token was
  created, when I enter fully compliant details and click “Submit”, then I am redirected to the login page with a message telling me “Reset password link has expired”, and the password is not updated
    Given I am on the reset password form
    And 10 minutes have passed since the reset token was created
    When I enter fully compliant details
    And I hit the save button
    Then I am redirected to the login page with a message telling me "Reset password link has expired"
    And the password is not updated

  @U8_AC10
  Scenario: Given I receive reset password link, and 10 minutes have passed since the reset token was created, when I click on the link, then I am redirected to the login page with a message telling me “Reset password link has expired”
    Given I receive reset password link
    And 10 minutes have passed since the reset token was created
    When I click on the link
    Then I am redirected to the login page with a message telling me "Reset password link has expired"
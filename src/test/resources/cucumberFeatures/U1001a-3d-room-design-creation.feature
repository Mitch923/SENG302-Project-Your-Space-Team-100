Feature: As Kaia, I can add a default 3d design to my renovation record, so I can visualise my renovation.

  Background:
    Given The system contains the default data
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U1001a_AC13
  Scenario: Given I am on the editor page, when I select a room from the rooms drop-down menu and click save, then the room is applied to the design I was editing.
    Given I am on the design editor page
    When I select a room from the rooms drop-down menu
    And I click to save the design
    Then The room is applied to the design I was editing
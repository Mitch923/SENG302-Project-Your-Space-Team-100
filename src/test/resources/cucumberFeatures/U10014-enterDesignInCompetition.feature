Feature: U10014 - As Inaya, I want to be able to choose to enter one of my existing designs into the weekly competition or create a brand new one for the competition, so that other users can view my design in the competition.

  Background:
    Given The system contains the default data
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U10014_AC3
  Scenario: AC3 - Given I see the create or import modal, when I click “Create”, then a new design is created for my entry into the competition, and I am taken to the editor page for my design entry.
    Given I see the create or import modal
    When I click Create
    Then A new design is created for my entry in the competition
    And I am taken to the editor page for my design entry
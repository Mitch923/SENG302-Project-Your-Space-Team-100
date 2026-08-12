Feature: U1006b - As Inaya, I can upload my own custom textures to Your Space, so I can personalise my designs with objects that match my style and preferences.

  Background:
    Given The system contains the default data
    And I am logged into the account with email "john@example.com" and password "P4$$word"

  @U1006b_AC2
  Scenario: U1006b AC2 - Given I have opened the file picker for the textures, when I select an individual texture file and click upload, then the texture is added to my repository of custom textures.
    Given I am on the design editor page
    And I have the file picker for textures open
    When I select an individual texture file and click upload
    Then the texture is added to my repository of custom textures

  @U1006b_AC3
  Scenario: U1006b AC3 - Given I am editing a design, when I open the upload textures tab, then I can select all previously uploaded textures or upload a new one.
    Given I am on the design editor page
    When I open the textures tab
    Then I can see a list of textures uploaded in the past.
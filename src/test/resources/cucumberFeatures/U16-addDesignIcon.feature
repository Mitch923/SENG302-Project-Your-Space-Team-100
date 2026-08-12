Feature: As Kaia, I want to add icon to my designs so that I can identify them quickly in my renovation record.


  @U16_AC2
  Scenario: Given I can see the available system icons, when I select an icon from the available systems icons , then the chosen icon is displayed together with the design everywhere on the system.
    Given I can see the available system icons
    When I select an icon from the available systems icons
    Then the chosen icon is displayed together with the design everywhere on the system
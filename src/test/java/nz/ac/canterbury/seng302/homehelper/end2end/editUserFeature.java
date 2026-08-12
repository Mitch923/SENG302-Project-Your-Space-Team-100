package nz.ac.canterbury.seng302.homehelper.end2end;

import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class editUserFeature {

    private final String newFirstName = "Donald";
    private final String newLastName = "Trump";
    private final String newEmail = "d.trump@example.com";

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void setup() {
        String originalPassword = "P4$$word";
        String originalFirstName = "Sally";
        String originalLastName = "Smith";
        String originalEmail = "sally@example.com";
        User testUser = new User(originalEmail, passwordEncoder.encode(originalPassword),
                originalFirstName,
                originalLastName);
        userService.verifyUser(testUser);
        userRepository.save(testUser);
        PlaywrightCucumberTest.page.navigate(PlaywrightCucumberTest.baseUrl + "/login");
        PlaywrightCucumberTest.page.locator("#email").fill(originalEmail);
        PlaywrightCucumberTest.page.locator("#password").fill(originalPassword);
        PlaywrightCucumberTest.page.locator("#signInBtn").click();
        PlaywrightCucumberTest.page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @Given("I am editing my user profile")
    public void iAmEditingMyUserProfile() {
        PlaywrightCucumberTest.page.navigate(PlaywrightCucumberTest.baseUrl + "/profile/edit");
    }

    @Given("I enter valid values for first name, last name, and email")
    public void iEnterValidValuesForFirstNameLastNameAndEmail() {
        PlaywrightCucumberTest.page.locator("#firstname").fill(newFirstName);
        PlaywrightCucumberTest.page.locator("#lastname").fill(newLastName);
        PlaywrightCucumberTest.page.locator("#email").fill(newEmail);
    }

    @When("I submit my changes")
    public void iSubmitMyChanges() {
        PlaywrightCucumberTest.page.locator("#submitButton").click();
    }

    @Then("I am taken back to my profile page")
    public void iAmTakenBackToMyProfilePage() {
        PlaywrightCucumberTest.page.waitForLoadState(LoadState.NETWORKIDLE);
        String currentUrl = PlaywrightCucumberTest.page.url();
        Assertions.assertTrue(currentUrl.contains("/profile"));
    }

    @Then("My details have been updated")
    public void myDetailsHaveBeenUpdated() {
        String displayedName = PlaywrightCucumberTest.page.locator("#name").locator("h1")
                .innerHTML();
        String displayedEmail = PlaywrightCucumberTest.page.locator("#email").locator("h4")
                .innerHTML();
        Assertions.assertEquals(newEmail, displayedEmail);
        Assertions.assertEquals(newFirstName + " " + newLastName, displayedName);
    }

}

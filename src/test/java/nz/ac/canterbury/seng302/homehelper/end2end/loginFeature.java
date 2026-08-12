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
public class loginFeature {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void setup() {
        User testUser = new User("john@example.com", passwordEncoder.encode("P4$$word"), "John",
                "");
        userService.verifyUser(testUser);
        userRepository.save(testUser);
    }

    @When("I enter the email {string} and password {string}")
    public void i_enter_the_email_and_password(String email, String password) {
        PlaywrightCucumberTest.page.locator("#email").fill(email);
        PlaywrightCucumberTest.page.locator("#password").fill(password);

    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        PlaywrightCucumberTest.page.navigate(PlaywrightCucumberTest.baseUrl + "/login");
    }

    @When("I click the sign in button")
    public void i_click_the_sign_in_button() {
        PlaywrightCucumberTest.page.locator("#signInBtn").click();
    }

    @Then("I am redirected to the main page")
    public void i_am_redirected_to_the_main_page() {
        PlaywrightCucumberTest.page.waitForLoadState(LoadState.NETWORKIDLE);
        Assertions.assertTrue(
                PlaywrightCucumberTest.page.url().contains("/home"));
    }


}

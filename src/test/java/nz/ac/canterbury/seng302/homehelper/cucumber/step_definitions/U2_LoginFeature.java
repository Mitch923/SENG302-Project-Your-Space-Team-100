package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.cucumber.hooks.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class U2_LoginFeature {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    private String email;
    private String password;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TestContext testContext;

    @PostConstruct
    public void setup() {
        User testUser = new User("john@example.com", passwordEncoder.encode("P4$$word"), "John",
                "");
        userService.verifyUser(testUser);
        userRepository.save(testUser);
    }

    @Given("There is no user logged in")
    public void there_is_no_user_logged_in() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.getLoggedUser();
        });
    }

    @Given("I am on the login form")
    public void i_am_on_the_login_form() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("loginTemplate"));
    }

    @Given("I enter the email {string} and password {string}")
    public void i_enter_the_email_and_password(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Given("I enter the email {string}")
    public void i_enter_the_email_email(String email) {
        this.email = email;
    }

    @Given("I enter the password {string}")
    public void i_enter_the_password_password(String password) {
        this.password = password;
    }

    @When("I click the sign in button")
    public void i_click_the_sign_in_button() throws Exception {
        testContext.httpResponse = mockMvc.perform(
                        formLogin("/login").userParameter("email").user(email).password(password))
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @Then("I am redirected to the main page")
    public void i_am_redirected_to_the_main_page() {
        String redirectUrl = testContext.httpResponse.getResponse().getRedirectedUrl();
        Assertions.assertEquals("/home", redirectUrl);
        Assertions.assertEquals(302, testContext.httpResponse.getResponse().getStatus());
    }

    @When("I click the not registered link")
    public void i_click_the_not_registered_link() throws Exception {
        testContext.httpResponse = mockMvc.perform(get("/register")).andExpect(status().isOk())
                .andExpect(view().name("registerPage")).andReturn();
    }

    @Then("I am redirected to the registration page")
    public void i_am_redirected_to_the_registration_page() {
        String viewName = testContext.httpResponse.getModelAndView().getViewName();
        String url = testContext.httpResponse.getRequest().getRequestURI();
        Assertions.assertEquals("registerPage", viewName);
        Assertions.assertEquals("/register", url);
        Assertions.assertEquals(200, testContext.httpResponse.getResponse().getStatus());

    }

    @Then("the login error message appears")
    public void the_login_error_message_appears() {
        String url = testContext.httpResponse.getResponse().getRedirectedUrl();
        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.startsWith("/login?error=true"));
    }
}

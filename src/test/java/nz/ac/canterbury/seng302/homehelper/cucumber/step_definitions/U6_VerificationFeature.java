package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.controller.VerificationController;
import nz.ac.canterbury.seng302.homehelper.entity.Token;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import nz.ac.canterbury.seng302.homehelper.service.SpringEmailService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Step definitions for U6 - Registration email, feature file:
 * /test/resources/features/U6-verification.feature Note: These tests are relying on the fact that
 * it can manage to perform all the steps to log in before one second has passed. Because the user
 * account gets deleted after one second instead of 10 minutes for testing purposes.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U6_VerificationFeature {

    @Autowired
    private UserService userService;
    @Autowired
    private RegisterController registerController;
    @Autowired
    private VerificationController verificationController;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    private SpringEmailService springEmailService;
    @Autowired
    private EmailVerificationService emailVerificationService;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private MockMvc mockMvcRegistration;
    private MockMvc mockMvcVerification;
    private Token token;
    private Long userId;
    private MvcResult httpResponse;
    @Autowired
    private MockMvc mockMvc;

    @PostConstruct
    public void setup() {
        springEmailService = Mockito.mock(SpringEmailService.class);
        registerController = new RegisterController(userService, emailVerificationService,
                springEmailService);
        mockMvcRegistration = MockMvcBuilders.standaloneSetup(registerController).build();
        verificationController = new VerificationController(userService, emailVerificationService);
        mockMvcVerification = MockMvcBuilders.standaloneSetup(verificationController).build();
    }

    @Given("I register to Your Space with valid registration details")
    public void i_register_to_home_helper_with_valid_registration_details() {
        firstName = "Elvis";
        lastName = "Presley";
        email = "elvis.presley@email.com";
        password = "Password1!";
    }

    @When("I click the Register button")
    public void i_click_the_register_button() throws Exception {
        httpResponse = mockMvcRegistration.perform(post("/register")
                .param("firstname", firstName)
                .param("lastname", lastName)
                .param("email", email)
                .param("password", password)
                .param("confirm", password)
        ).andReturn();
        userId = userRepository.findByEmailIgnoreCase(email).get().getId();
    }

    @When("I wait for 10 minutes")
    public void i_wait_for_10_minutes() throws InterruptedException {
        // But its actually 1.5 seconds
        await().atMost(3L, TimeUnit.SECONDS)
                .until(() -> emailVerificationService.getScheduledRegisterTasks().isEmpty());
    }

    @When("I submit my code")
    public void i_submit_my_code() throws Exception {
        String tokenString = token.getToken().replaceAll(" ", "");
        httpResponse = mockMvcVerification.perform(post("/verification?userId=" + userId)
                        .param("verificationCode", tokenString))
                .andReturn();
    }

    @When("I submit an unknown code")
    public void i_submit_an_unknown_code() throws Exception {
        String tokenString = token.getToken().replaceAll(" ", "");
        // Add a 1 to the end of the token so that it does not match the correct code
        tokenString = tokenString + "1";
        httpResponse = mockMvcVerification.perform(post("/verification?userId=" + userId)
                        .param("verificationCode", tokenString))
                .andReturn();
    }

    @When("I log in to the system")
    public void i_log_in_to_the_system() throws Exception {
        httpResponse = mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", email)
                .param("password", password)
        ).andReturn();
    }

    @Then("A unique signup code is generated for the email")
    public void a_unique_signup_code_is_generated_for_the_email() {
        token = tokenRepository.getByUserId(userId);
        Assertions.assertNotNull(token);
    }

    @Then("A confirmation email is sent to my email address")
    public void a_confirmation_email_is_sent_to_my_email_address() {
        verify(springEmailService).sendSignUpEmail(firstName, email, token.getToken());
    }

    @Then("I am sent to a page asking for the signup code")
    public void i_am_sent_to_a_page_asking_for_the_signup_code() {
        Assertions.assertTrue(httpResponse.getResponse().getStatus() >= 300);
        Assertions.assertTrue(httpResponse.getResponse().getStatus() < 400);
        String redirection = httpResponse.getResponse().getRedirectedUrl();
        Assertions.assertEquals("/verification?userId=" + userId, redirection);
    }

    @Then("The account is deleted")
    public void the_account_is_deleted() {
        Assertions.assertFalse(userRepository.findByEmailIgnoreCase(email).isPresent());
    }

    @Then("The code is deleted")
    public void the_code_is_deleted() {
        Assertions.assertNull(tokenRepository.getByUserId(userId));
    }

    @Then("The system validates my code successfully")
    public void the_system_validates_my_code_successfully() {
        User user = userRepository.findByEmailIgnoreCase(email).get();
        boolean userVerified = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList().contains("ROLE_USER");
        Assertions.assertTrue(userVerified);
    }

    @Then("I get redirected to the login page")
    public void i_get_redirected_to_the_login_page() {
        Assertions.assertTrue(httpResponse.getResponse().getStatus() >= 300);
        Assertions.assertTrue(httpResponse.getResponse().getStatus() < 400);
        Assertions.assertTrue(httpResponse.getResponse().getRedirectedUrl().contains("/login"));
    }

    @Then("An error message: Signup code invalid, is displayed")
    public void an_error_message_is_displayed_signup_code_is_invalid() {
        Assertions.assertTrue(httpResponse.getResponse().getRedirectedUrl()
                .contains("/verification"));
        Assertions.assertNotNull(httpResponse.getFlashMap().get("error"));
        Assertions.assertEquals("Signup code invalid",
                httpResponse.getFlashMap().get("error"));
    }

    @Then("My account is not verified")
    public void my_account_is_not_verified() {
        User user = userRepository.findByEmailIgnoreCase(email).get();
        boolean userVerified = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList().contains("ROLE_USER");
        Assertions.assertFalse(userVerified);
    }
}

package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U0_LogoutFeature {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    AuthenticationProvider authenticationProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailVerificationService emailVerificationService;

    private MvcResult result;


    //Background
    @Given("There is a user with details {string}, {string}, {string}, {string} who is logged in")
    public void thereIsAUserWithNameWhoIsLoggedIn(String firstName, String lastName, String email,
            String password) throws Exception {
        String encodePassword = passwordEncoder.encode(password);
        User sarah = new User(email, encodePassword, firstName, lastName);
        sarah.revokeAuthority("ROLE_UNVERIFIED");
        sarah.grantAuthority("ROLE_USER");
        userRepository.save(sarah);
        emailVerificationService.cancelUserDeletion(sarah);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, password
        );
        Authentication authenticated = authenticationProvider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
    }

    // AC2, AC4
    @When("I click logout")
    public void iClickLogout() throws Exception {
        result = mockMvc.perform(post("/logout").with(csrf())).andReturn(); // No assertions yet
    }

    // AC4
    @Then("I am redirected to the landing page")
    public void iAmRedirectedToTheHomePage() throws Exception {
        int status = result.getResponse().getStatus();
        String redirectUrl = result.getResponse().getRedirectedUrl();

        Assertions.assertTrue(status >= 300 && status < 400,
                "Expected a redirection status, got: " + status);
        Assertions.assertEquals("/", redirectUrl, "Expected to be redirected to home");
    }

    //AC2
    @And("I try access a page that requires logging in")
    public void iTryAccessAPageThatRequiresLoggingIn() throws Exception {
        result = mockMvc.perform(get("/profile")).andReturn(); // Save again for later use
    }

    //AC2
    @Then("I am redirected to the login page")
    public void iAmRedirectedToTheLoginPage() throws Exception {
        int status = result.getResponse().getStatus();
        String redirectUrl = result.getResponse().getRedirectedUrl();

        Assertions.assertEquals(302, status, "Expected redirect status 302");
        Assertions.assertTrue(redirectUrl != null && redirectUrl.contains("/login"),
                "Expected to be redirected to login page, got: " + redirectUrl);
    }
}


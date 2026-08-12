package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import nz.ac.canterbury.seng302.homehelper.service.SpringEmailService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class U8_ResetPasswordFeature {

    private final ArgumentCaptor<String> passwordResetTokenCaptor = ArgumentCaptor.forClass(
            String.class);
    @Autowired
    private UserService userService;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private SpringEmailService springEmailService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntityManager entityManager;
    private User user;
    private String emailAddress;
    private MvcResult mvcResult;
    private String password;
    private String newPassword;
    private String confirmNewPassword;

    @PostConstruct
    public void beforeScenario() {
        Mockito.reset(springEmailService);
        doNothing().when(springEmailService).sendResetPasswordEmail(Mockito.anyString());
    }

    @Given("There is a user with details {string}, {string}, {string}, {string} who is not logged in.")
    public void thereIsAUserWithDetailsWhoIsNotLoggedIn(String firstName, String lastName,
            String email,
            String password) throws InterruptedException {
        user = new User(email, password, firstName, lastName);

        Thread.sleep(1000);
        userService.saveUser(user);
        emailAddress = email;
        this.password = password;
    }

    @Given("I am on the lost password form")
    public void iAmOnTheLostPasswordForm() {
        // No setup
    }

    @And("I enter an empty or {string}")
    public void iEnterAnEmptyOrMalformedEmailAddress(String malformedEmailAddress) {
        this.emailAddress = malformedEmailAddress;
    }

    @When("I click the Submit button")
    public void iClickTheSubmitButton() throws Exception {
        this.mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/reset")
                        .param("email", emailAddress)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andReturn();
    }

    @And("I enter a valid email that is not known to the system")
    public void iEnterAValidEmailThatIsNotKnownToTheSystem() {
        emailAddress = "unknown@email.com";
    }

    @Then("a confirmation message tells me {string}")
    public void aConfirmationMessageTellsMe(String message) {
        var model = Objects.requireNonNull(mvcResult.getModelAndView()).getModel();
        assertEquals(message, model.get("success"));
    }

    @And("I enter an email that is known to the system")
    public void iEnterAnEmailThatIsKnownToTheSystem() {
        emailAddress = user.getEmail();
    }

    @And("an email is sent to the email address with a link containing a unique reset token to update the password of the profile associated to that email.")
    public void anEmailIsSentToTheEmailAddressWithALinkContainingAUniqueResetTokenToUpdateThePasswordOfTheProfileAssociatedToThatEmail() {
        assertEquals(200, this.mvcResult.getResponse().getStatus());
        verify(springEmailService, times(1)).sendResetPasswordEmail(emailAddress);
    }

    @Given("I received an email to reset my password")
    public void iReceivedAnEmailToResetMyPassword() throws Exception {

        this.mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/reset")
                        .param("email", emailAddress)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andReturn();

        verify(emailVerificationService, times(1)).scheduleResetPasswordTokenRevocation(
                passwordResetTokenCaptor.capture());
    }

    @When("I go to the given URL passed in the email")
    public void iGoToTheGivenURLPassedInTheEmail() throws Exception {
        this.mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/reset")
                        .param("token", passwordResetTokenCaptor.getValue())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andReturn();
    }


    @Then("I am asked to supply a new password with “new password” and “retype password” fields.")
    public void iAmAskedToSupplyANewPasswordWithNewPasswordAndRetypePasswordFields() {
        // This is UI
    }


    @Given("I am on the reset password form")
    public void iAmOnTheResetPasswordForm() throws Exception {
        iReceivedAnEmailToResetMyPassword(); // Same functionality
        iGoToTheGivenURLPassedInTheEmail();
    }

    @And("I enter two different passwords in “new password” and “retype password” fields")
    public void iEnterTwoDifferentPasswordsInNewPasswordAndRetypePasswordFields() {
        this.newPassword = "password1";
        this.confirmNewPassword = "password2";
    }


    @When("I hit the save button")
    public void iHitTheSaveButton() throws Exception {
        this.mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/reset")
                        .param("token", passwordResetTokenCaptor.getValue())
                        .param("password", newPassword)
                        .param("confirm", confirmNewPassword)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andReturn();
    }

    @Then("an error message on the model says {string}")
    public void anErrorMessageTellsMe(String message) {
        Map<String, String> errors = (Map<String, String>) mvcResult.getModelAndView().getModel()
                .get("errors");
        assertTrue(errors.containsValue(message));
    }

    @And("the password is not updated")
    public void thePasswordIsNotUpdated() {
        user = userRepo.findByEmailIgnoreCase(emailAddress).get();
        assertEquals(password, user.getPassword());
    }

    @And("I enter a weak password")
    public void iEnterAWeakPassword() {
        newPassword = "cat";
        confirmNewPassword = "cat";
    }

    @When("I enter fully compliant details")
    public void iEnterFullyCompliantDetails() {
        newPassword = "$trongP4$$word1";
        confirmNewPassword = "$trongP4$$word1";
    }

    @Then("my password is updated")
    public void myPasswordIsUpdated() {
        user = userRepo.findByEmailIgnoreCase(user.getEmail()).get();
        password = user.getPassword();
        assertTrue(passwordEncoder.matches(newPassword, password));
    }

    @And("an email is sent to my email address to confirm that my password has been updated")
    public void anEmailIsSentToMyEmailAddressToConfirmThatMyPasswordHasBeenUpdated() {
        assertEquals(302, this.mvcResult.getResponse().getStatus());
        verify(springEmailService, times(1)).sendResetPasswordEmail(emailAddress);
    }

    @And("I am taken to the login page")
    public void iAmRedirectedToTheLoginPage() {
        assertEquals("/login", mvcResult.getResponse().getRedirectedUrl());
    }

    @And("{int} minutes have passed since the reset token was created")
    public void minutesHavePassedSinceTheResetTokenWasCreated(int minutes) {
        // We will be using 1 second instead of minutes for the sake of our sanity
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> emailVerificationService.getScheduledPasswordResetTasks().isEmpty());
    }

    @Then("I am redirected to the login page with a message telling me {string}")
    public void iAmRedirectedToTheLoginPageWithAMessageTellingMe(String message) {
        iAmRedirectedToTheLoginPage();
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(mvcResult.getRequest());
        assertTrue(flashMap.containsKey("error"));
        assertEquals(message, flashMap.get("error"));
    }

    @Given("I receive reset password link")
    public void iReceiveResetPasswordLink() throws Exception {
        iReceivedAnEmailToResetMyPassword();
    }

    @When("I click on the link")
    public void iClickOnTheLink() throws Exception {
        iGoToTheGivenURLPassedInTheEmail();
    }
}

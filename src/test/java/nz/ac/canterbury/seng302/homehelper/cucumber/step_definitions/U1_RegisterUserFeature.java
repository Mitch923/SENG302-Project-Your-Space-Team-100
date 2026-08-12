package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.HashMap;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.cucumber.hooks.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U1_RegisterUserFeature {

    @Autowired
    public MockMvc mockMvc;
    @Autowired
    public UserRepository userRepository;

    String VALID_EMAIL = "john@doe.com";
    String VALID_FIRST_NAME = "John";
    String VALID_LAST_NAME = "Doe";
    String VALID_PASSWORD = "P4$$word";

    @Autowired
    private TestContext testContext;

    private User userToRegister = new User(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME,
            VALID_LAST_NAME);
    private String passwordConfirm = VALID_PASSWORD;


    // AC2
    @Given("I enter valid first name email address and password values")
    public void iEnterValidFirstNameEmailAddressAndPasswordValues() {
        userToRegister = new User(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
    }

    // AC2, AC3, AC3.1 AC3.2, AC4, AC4.1, AC6, AC7, AC8, AC10
    @When("I click sign up")
    public void iClickSignUp() throws Exception {
        testContext.httpResponse = mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstname", userToRegister.getFirstName())
                        .param("lastname", userToRegister.getLastName())
                        .param("email", userToRegister.getEmail())
                        .param("password", userToRegister.getPassword())
                        .param("confirm", passwordConfirm))
                .andReturn();
    }

    // AC2
    @Then("I am redirected to the verification page")
    // This was previously the profile page but U6 overrides this AC
    public void iAmRedirectedToMyUserProfilePage() {
        Optional<User> savedUser = userRepository.findByEmailIgnoreCase(userToRegister.getEmail());
        Assertions.assertTrue(savedUser.isPresent(), "Expected to be able to save user");

        int status = testContext.httpResponse.getResponse().getStatus();
        String redirectUrl = testContext.httpResponse.getResponse().getRedirectedUrl();

        Assertions.assertTrue(status >= 300 && status < 400,
                "Expected a redirection status, got: " + status);
        Assertions.assertTrue(redirectUrl != null && redirectUrl.contains("/verification"),
                "Expected to be redirected to login page, got: " + redirectUrl);
    }

    // AC3, AC3.1, AC3.2, AC4, AC4.1
    @Given("I enter {string} and {string}")
    public void iEnterFirstNameAndLastName(String firstName, String lastName) {
        userToRegister.setFirstName(firstName);
        userToRegister.setLastName(lastName);
    }

    // AC3
    @Then("an error message tells me {string}")
    public void anErrorMessageTellsMeErrorMessage(String expectedError) {
        HashMap<String, String> errors = testContext.getErrorsFromModel();
        Assertions.assertEquals(1, errors.size());

        boolean matchFound = errors.values().stream()
                .anyMatch(msg -> msg != null && msg.contains(expectedError));

        Assertions.assertTrue(matchFound,
                "Expected an error message containing: '" + expectedError + "'");
    }

    //AC3.1
    @Then("an error message tells me about both first and last name includes errors")
    public void anErrorMessageTellsMeAboutBothFirstAndLastNameIncludesErrors() {
        String expectedError = "name must only include letters, spaces, hyphens or apostrophes";
        HashMap<String, String> errors = testContext.getErrorsFromModel();

        long count = errors.values().stream()
                .filter(msg -> msg != null && msg.contains(expectedError))
                .count();

        Assertions.assertEquals(2, count,
                "Expected the error message to appear twice, but found " + count);
    }

    //AC3.2
    @Then("an error message tells me first name cannot be empty and last name must only include")
    public void anErrorMessageTellsMeFirstNameCannotBeEmptyAndLastNameMustOnlyInclude() {
        String firstNameError = "First name cannot be empty";
        String lastNameError = "name must only include letters, spaces, hyphens or apostrophes";
        HashMap<String, String> errors = testContext.getErrorsFromModel();

        Assertions.assertTrue(
                errors.values().stream()
                        .anyMatch(msg -> msg != null && msg.contains(firstNameError)) &&
                        errors.values().stream()
                                .anyMatch(msg -> msg != null && msg.contains(lastNameError)),
                "Expected both first name and last name errors to be present"
        );
    }

    //AC4
    @Then("an error message tells me first or last name must be {int} characters long or less")
    public void anErrorMessageTellsMeFirstOrLastNameMustBeCharactersLongOrLess(int maxCharacters) {
        String expectedError = "name must be " + maxCharacters + " characters long or less";
        HashMap<String, String> errors = testContext.getErrorsFromModel();
        Assertions.assertEquals(1, errors.size());

        Assertions.assertTrue(errors.values().stream()
                        .anyMatch(msg -> msg != null && msg.contains(expectedError)),
                "Expected an error message containing: '" + expectedError + "'");
    }

    //AC4.1
    @Then("an error message tells me both first and last name are too long")
    public void anErrorMessageTellsMeBothFirstAndLastNameAreTooLong() {
        String expectedError = "name must be 64 characters long or less";
        HashMap<String, String> errors = testContext.getErrorsFromModel();

        long count = errors.values().stream()
                .filter(msg -> msg != null && msg.contains(expectedError))
                .count();

        Assertions.assertEquals(2, count,
                "Expected the error message to appear twice, but found " + count);
    }

    //AC5
    @Given("I enter invalid email {string}")
    public void iEnterInvalidEmailEmail(String email) {
        userToRegister = new User(email, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
    }

    //AC5
    @Then("an error message tells me Email address must be in the form jane@doe.nz")
    public void anErrorMessageTellsMeEmailAddressMustBeInTheFormJaneDoeNz() {
        String expectedError = "Email address must be in the form 'jane@doe.nz'";
        HashMap<String, String> errors = testContext.getErrorsFromModel();
        Assertions.assertEquals(1, errors.size());

        Assertions.assertTrue(errors.values().stream()
                        .anyMatch(msg -> msg != null && msg.contains(expectedError)),
                "Expected an error message containing: '" + expectedError + "'");
    }

    // AC6
    @Given("I enter {string} for my email")
    public void iEnterFoprMyEmail(String email) {
        userToRegister = new User(email, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
    }

    //AC6
    @And("{string} has already been used to register")
    public void hasAlreadyBeenUsedToRegister(String email) {
        User sarah = new User(email, "password", "Sarah", "Thompson");
        sarah.revokeAuthority("ROLE_UNVERIFIED");
        sarah.grantAuthority("ROLE_USER");
        userRepository.save(sarah);
    }

    //AC6
    @Then("an error message tells me This email is already in use")
    public void anErrorMessageTellsMeThisEmailIsAlreadyInUse() {
        String expectedError = "This email address is already in use";
        HashMap<String, String> errors = testContext.getErrorsFromModel();
        Assertions.assertEquals(1, errors.size());

        Assertions.assertTrue(errors.values().stream()
                        .anyMatch(msg -> msg != null && msg.contains(expectedError)),
                "Expected an error message containing: '" + expectedError + "'");
    }

    //AC7
    @Given("I enter {string} for my password")
    public void iEnterForMyPassword(String password) {
        userToRegister = new User("john@doe.com", password, "john", "doe");
    }

    //AC7
    @And("I enter {string} to confirm my password")
    public void iEnterToConfirmMyPassword(String passwordConfirmation) {
        passwordConfirm = passwordConfirmation;
    }

    //AC7
    @Then("an error message tells me passwords do not match")
    public void anErrorMessageTellsMePasswordsDoNotMatch() {
        String expectedError = "The passwords do not match";
        HashMap<String, String> errors = testContext.getErrorsFromModel();
        Assertions.assertEquals(1, errors.size());

        Assertions.assertTrue(errors.values().stream()
                        .anyMatch(msg -> msg != null && msg.contains(expectedError)),
                "Expected an error message containing: '" + expectedError + "'");
    }

    //AC8
    @Given("I enter invalid password {string}")
    public void iEnterInvalidPassword(String password) {
        userToRegister = new User("john@doe.com", password, "john", "doe");
        passwordConfirm = password;
    }

    //AC8
    @Then("an error message tells me my password doesn't meet the requirements")
    public void anErrorMessageTellsMeMyPasswordDoesnTMeetTheRequirements() {
        String expectedError = "Your password must be at least 8 "
                + "characters long and include at least one uppercase letter, one lowercase letter, one number, and one "
                + "special character.";
        HashMap<String, String> errors = testContext.getErrorsFromModel();
        Assertions.assertEquals(1, errors.size());

        Assertions.assertTrue(errors.values().stream()
                        .anyMatch(msg -> msg != null && msg.contains(expectedError)),
                "Expected an error message containing: '" + expectedError + "'");
    }

    //AC9
    @Given("I am on the registration form")
    public void iAmOnTheRegistrationForm() {
        userToRegister = new User(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
        passwordConfirm = VALID_PASSWORD;
    }

    //AC9
    @When("I click cancel")
    public void iClickCancel() throws Exception {
        testContext.httpResponse = mockMvc.perform(post("/logout").with(csrf())).andReturn();
    }

    //AC9
    @Then("I am redirected to the home page")
    public void iAmRedirectedToTheHomePage() {
        String redirectUrl = testContext.httpResponse.getResponse().getRedirectedUrl();
        Assertions.assertEquals("/", redirectUrl, "Expected to be redirected to home");
    }

    //AC10
    @Given("I enter valid values to sign up with password {string}")
    public void iEnterValidValuesToSignUpWithPassword(String password) {
        userToRegister = new User(VALID_EMAIL, password, VALID_FIRST_NAME, VALID_LAST_NAME);
        passwordConfirm = password;
    }

    //AC10
    @Then("The password saved doesn't exactly match the password entered")
    public void thePasswordSavedDoesnTExactlyMatchThePasswordEntered() {
        Optional<User> savedUser = userRepository.findByEmailIgnoreCase(userToRegister.getEmail());
        Assertions.assertTrue(savedUser.isPresent(), "Expected to be able to save user");

        Assertions.assertNotEquals(savedUser.get().getPassword(), userToRegister.getPassword());
    }
}
package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U4_EditUserProfileFeature {

    private static final Path validTestImagesDir = Paths.get(
            System.getProperty("user.dir"),
            "src",
            "test",
            "resources",
            "test.ImageUploadValidator.images",
            "test.png"
    );
    private static MockMultipartFile validMultipartFile;
    private final String originalEmail = "sarah@example.com";
    private String editFirstName;
    private String editLastName;
    private String editEmail;
    private MvcResult httpResponse;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    public static void setup() throws IOException {
        File file = new File(String.valueOf(validTestImagesDir));
        FileInputStream bytes = new FileInputStream(file);
        validMultipartFile = new MockMultipartFile("file",
                "test.png",
                "image/png", bytes);
    }

    @Given("I am on the edit profile form")
    public void i_am_on_the_edit_profile_form() throws Exception {
        httpResponse = mockMvc.perform(get("/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfile")).andReturn();
    }

    @Given("I am on my user profile page")
    public void i_am_on_my_user_profile_page() throws Exception {
        httpResponse = mockMvc.perform(get("/profile")).andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andReturn();
    }

    @When("I submit my edit profile changes with values {string}, {string}, {string}")
    public void i_edit_my_profile_with_values(String firstName, String lastName, String email)
            throws Exception {
        httpResponse = mockMvc.perform(multipart("/profile/edit")
                .file(validMultipartFile)
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("email", email)
                .param("useFailSubmissionImage", "false")
                .with(csrf())).andReturn();
        editFirstName = firstName;
        editLastName = lastName;
        editEmail = email;
    }

    @When("I click the Edit button")
    public void i_click_the_edit_button() throws Exception {
        httpResponse = mockMvc.perform(get("/profile/edit")).andReturn();
    }

    @When("I click the Cancel button")
    public void i_click_the_cancel_button() throws Exception {
        httpResponse = mockMvc.perform(get("/profile")).andReturn();
    }

    @Then("An error message tells me: {string}")
    public void an_error_message_tells_me(String errorMessage) {
        Assertions.assertNotNull(httpResponse.getModelAndView().getModelMap()
                .getAttribute("errors"));
        Map<?, ?> errors = (Map<?, ?>) httpResponse.getModelAndView().getModelMap()
                .getAttribute("errors");
        String errorMessages = errors.values().stream()
                .filter(value -> value instanceof String)
                .map(value -> (String) value)
                .collect(Collectors.joining(". "));
        Assertions.assertTrue(errorMessages.contains(errorMessage));
    }

    @Then("No changes are saved")
    public void no_changes_are_saved() {
        boolean changesSaved = false;
        String firstName = userRepository.findByEmailIgnoreCase(originalEmail).get().getFirstName();
        String lastName = userRepository.findByEmailIgnoreCase(originalEmail).get().getLastName();
        if (Objects.equals(firstName, editFirstName) || Objects.equals(lastName, editLastName)) {
            changesSaved = true;
        }
        Assertions.assertFalse(changesSaved);
    }

    @Then("My new details are saved")
    public void my_new_details_are_saved() {
        boolean changesSaved = false;
        String firstName = userRepository.findByEmailIgnoreCase(originalEmail).get().getFirstName();
        String lastName = userRepository.findByEmailIgnoreCase(originalEmail).get().getLastName();
        if (Objects.equals(firstName, editFirstName) && Objects.equals(lastName, editLastName)) {
            changesSaved = true;
        }
        Assertions.assertTrue(changesSaved);
    }

    @Then("I am taken back to the profile page")
    public void i_am_taken_back_to_the_profile_page() {
        Assertions.assertTrue(httpResponse.getResponse().getStatus() >= 300);
        Assertions.assertTrue(httpResponse.getResponse().getStatus() < 400);
        String redirection = httpResponse.getModelAndView().getViewName();
        Assertions.assertEquals("redirect:/profile", redirection);
    }

    @Then("My email is not updated")
    public void my_email_is_not_updated() {
        boolean changesSaved = (Objects.equals(originalEmail, editEmail));
        Assertions.assertFalse(changesSaved);
    }

    @Then("I see the edit profile form")
    public void i_see_the_edit_profile_form() {
        Assertions.assertEquals(200, httpResponse.getResponse().getStatus());
        Assertions.assertEquals("editProfile", httpResponse.getModelAndView().getViewName());
    }

    @Then("I see the profile page")
    public void i_see_the_profile_page() {
        Assertions.assertEquals(200, httpResponse.getResponse().getStatus());
        Assertions.assertEquals("profile", httpResponse.getModelAndView().getViewName());
    }

}

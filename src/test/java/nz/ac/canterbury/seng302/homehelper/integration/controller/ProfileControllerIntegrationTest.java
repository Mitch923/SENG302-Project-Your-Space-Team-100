package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.hamcrest.Matchers.hasEntry;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import nz.ac.canterbury.seng302.homehelper.controller.ProfileController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.SpringEmailService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
public class ProfileControllerIntegrationTest {

    private static final Path validTestImagesDir = Paths.get(
            System.getProperty("user.dir"),
            "src",
            "test",
            "resources",
            "test.ImageUploadValidator.images"
    );
    String VALID_FIRST_NAME = "John";
    String VALID_LAST_NAME = "Doe";
    String VALID_EMAIL = "john.doe@gmail.com";
    String VALID_PASSWORD = "P4$$word";
    @Autowired
    private ProfileController profileController;
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HttpSession httpSession;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private SpringEmailService springEmailService;

    /**
     * Helper function modified from ChatGPT for using test files
     *
     * @param fileName name of test file in test.ImageUploadValidator.images
     * @return a MockMultiPartFile filled with data from the test file
     */
    public static MockMultipartFile getTestMultipartFileFromFile(String fileName,
            String contentType) throws IOException {
        Path filePath = validTestImagesDir.resolve(fileName);
        File file = new File(filePath.toString());
        FileInputStream input = new FileInputStream(file);

        return new MockMultipartFile(
                "testFile", // field name
                file.getName(), // original filename
                contentType, // content type
                input // file content
        );
    }

    /// Helper Functions

    @PostConstruct
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController).build();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
    }

    /**
     * Creates a user and logs into the authentication
     *
     * @param email             the desired email
     * @param plaintextPassword the desired password in plain text
     * @param firstName         the desired first name
     * @param lastName          the desired last name
     */
    public User createUserAndLogin(String email, String plaintextPassword, String firstName,
            String lastName) {
        // Create user
        User user = new User(email, passwordEncoder.encode(plaintextPassword), firstName, lastName);
        userService.saveUser(user);
        userService.verifyUser(user);

        // Authenticate user
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                plaintextPassword);
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        return user;
    }

    @Test
    public void editProfile_userLoggedIn_returnProfile() throws Exception {
        // Create User and Login
        createUserAndLogin(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        // Ensure logged in
        Assertions.assertTrue(userService.isLoggedIn());

        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfile"));
    }

    @Test
    public void updateProfile_validDetails_profileUpdated() throws Exception {
        String VALID_UPDATED_EMAIL = "uc@learn.ac.nz";
        String VALID_UPDATED_FIRST_NAME = "updatedFirstName";
        String VALID_UPDATED_LAST_NAME = "updatedLastName";
        createUserAndLogin(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        // Ensure logged in
        Assertions.assertTrue(userService.isLoggedIn());
        MockMultipartFile VALID_CONTENTS = getTestMultipartFileFromFile("test.png", "image/png");
        MockMultipartFile VALID_FILE = new MockMultipartFile(
                "file",                    // Name of the file input in your form
                "test.png",                // Filename
                "image/png",              // Content type of the file
                VALID_CONTENTS.getBytes()  // Content of the file
        );

        mockMvc.perform(multipart("/profile/edit")
                        .file(VALID_FILE)
                        .param("firstName", VALID_UPDATED_FIRST_NAME)
                        .param("lastName", VALID_UPDATED_LAST_NAME)
                        .param("email", VALID_UPDATED_EMAIL)
                        .param("useFailSubmissionImage", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile"));

        User currentLoggedUser = userService.getLoggedUser();
        Assertions.assertEquals(VALID_UPDATED_EMAIL, currentLoggedUser.getEmail());
        Assertions.assertEquals(VALID_UPDATED_FIRST_NAME, currentLoggedUser.getFirstName());
        Assertions.assertEquals(VALID_UPDATED_LAST_NAME, currentLoggedUser.getLastName());
    }

    @Test
    public void updateProfile_invalidDetails_profileNotUpdated() throws Exception {
        String INVALID_UPDATED_EMAIL = "uc@learn";
        String INVALID_UPDATED_FIRST_NAME = "updatedFirstName";
        String INVALID_UPDATED_LAST_NAME = "updatedLastName";
        createUserAndLogin(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        // Ensure logged in
        Assertions.assertTrue(userService.isLoggedIn());

        MockMultipartFile INVALID_FILE = new MockMultipartFile(
                "file",                    // Name of the file input in your form
                "test.ew",                // Filename
                "image/png",              // Content type of the file
                "This is a test file.".getBytes()  // Content of the file
        );
        mockMvc.perform(multipart("/profile/edit")
                        .file(INVALID_FILE)
                        .param("firstName", INVALID_UPDATED_FIRST_NAME)
                        .param("lastName", INVALID_UPDATED_LAST_NAME)
                        .param("email", INVALID_UPDATED_EMAIL)
                        .param("useFailSubmissionImage", "false"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("editProfile"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                        "imageUpload", "Image must be of type png, jpg or svg"
                )));

        User currentLoggedUser = userService.getLoggedUser();
        Assertions.assertEquals(VALID_EMAIL, currentLoggedUser.getEmail());
        Assertions.assertEquals(VALID_FIRST_NAME, currentLoggedUser.getFirstName());
        Assertions.assertEquals(VALID_LAST_NAME, currentLoggedUser.getLastName());
    }

    @Test
    public void editPassword_userLoggedIn_returnEditPasswordPage() throws Exception {
        // Create User and Login
        createUserAndLogin(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        // Ensure logged in
        Assertions.assertTrue(userService.isLoggedIn());

        mockMvc.perform(get("/profile/editPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("editPasswordPage"));
    }

    @Test
    public void editPassword_validDetails_passwordUpdated() throws Exception {
        String VALID_UPDATED_OLD_PASSWORD = VALID_PASSWORD;
        String VALID_UPDATED_PASSWORD = "Pa$$word123";
        String VALID_UPDATED_PASSWORD_CONFIRM = "Pa$$word123";
        User user = createUserAndLogin(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME,
                VALID_LAST_NAME);

        // Ensure logged in
        Assertions.assertTrue(userService.isLoggedIn());

        mockMvc.perform(post("/profile/editPassword")
                        .param("oldPassword", VALID_UPDATED_OLD_PASSWORD)
                        .param("password", VALID_UPDATED_PASSWORD)
                        .param("confirm", VALID_UPDATED_PASSWORD_CONFIRM))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile"));

        User currentLoggedUser = userService.getLoggedUser();
        Assertions.assertNotEquals(user.getPassword(), currentLoggedUser.getPassword());
    }

    @Test
    public void editPassword_invalidDetails_passwordNotUpdated() throws Exception {
        String INVALID_UPDATED_OLD_PASSWORD = "";
        String INVALID_UPDATED_PASSWORD = "";
        String INVALID_UPDATED_PASSWORD_CONFIRM = "";
        User user = createUserAndLogin(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME,
                VALID_LAST_NAME);

        // Ensure logged in
        Assertions.assertTrue(userService.isLoggedIn());

        mockMvc.perform(post("/profile/editPassword")
                        .param("oldPassword", INVALID_UPDATED_OLD_PASSWORD)
                        .param("password", INVALID_UPDATED_PASSWORD)
                        .param("confirm", INVALID_UPDATED_PASSWORD_CONFIRM))
                .andExpect(status().isOk())
                .andExpect(view().name("editPasswordPage"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute(
                        "errors", hasEntry("password",
                                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.")
                ))
                .andExpect(model().attribute(
                        "errors", hasEntry("oldPassword", "Your old password is incorrect")
                ));

        User currentLoggedUser = userService.getLoggedUser();
        Assertions.assertEquals(user.getPassword(), currentLoggedUser.getPassword());

    }
}

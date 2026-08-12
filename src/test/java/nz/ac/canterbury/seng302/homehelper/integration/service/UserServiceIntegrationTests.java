package nz.ac.canterbury.seng302.homehelper.integration.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class UserServiceIntegrationTests {

    @SpyBean
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpSession httpSession;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static Stream<Arguments> validImageTypes() {
        return Stream.of(
                Arguments.of("image/png", ".png"),
                Arguments.of("image/jpeg", ".jpg"),
                Arguments.of("image/svg", ".svg")
        );
    }

    @Test
    public void updateUserTest() {
        // Create target user and save
        User user = new User("asd@asd", "password", "firstName", "lastName");
        userService.saveUser(user);

        // Create user to perform update with
        User newUser = new User("newasd@asd", "newPassword", "newFirstName", "newLastName");

        // Perform update
        userService.updateUser(
                newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getEmail(),
                user.getEmail(),
                null
        );

        User updatedUser = userService.getUserByEmail(newUser.getEmail()).get();

        // Ensure not null
        Assertions.assertNotNull(updatedUser);

        // Ensure values are correct
        Assertions.assertEquals(newUser.getEmail(), updatedUser.getEmail());
        Assertions.assertEquals(user.getPassword(),
                updatedUser.getPassword()); // Should be unchanged
        Assertions.assertEquals(newUser.getFirstName(), updatedUser.getFirstName());
        Assertions.assertEquals(newUser.getLastName(), updatedUser.getLastName());
        Assertions.assertEquals(user.getId(), updatedUser.getId()); // Ensure ids stay the same

        // Ensure error throw on invalid user
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.updateUser(
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    "noemail",
                    null);
        });
    }

    @Test
    public void isLoggedInTest() {
        // Create user
        User user = new User("asd@asd", passwordEncoder.encode("password"), "firstName",
                "lastName");
        userService.verifyUser(user);
        userService.saveUser(user);

        // Authenticate user
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                "password");
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // Ensure logged in
        Assertions.assertTrue(userService.isLoggedIn());

        // Log out
        SecurityContextHolder.clearContext();

        // Ensure not logged in
        Assertions.assertFalse(userService.isLoggedIn());

    }

    @Test
    public void getLoggedUserTest() {
        // Create user
        User user = new User("asd@asd", passwordEncoder.encode("password"), "firstName",
                "lastName");
        userService.verifyUser(user);
        userService.saveUser(user);

        // Authenticate user
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                "password");
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // Ensure logged in user is correct
        User loggedUser = userService.getLoggedUser();
        Assertions.assertNotNull(loggedUser);
        Assertions.assertEquals(user.getEmail(), loggedUser.getEmail());
        Assertions.assertEquals(user.getPassword(), loggedUser.getPassword());
        Assertions.assertEquals(user.getFirstName(), loggedUser.getFirstName());
        Assertions.assertEquals(user.getLastName(), loggedUser.getLastName());

        // Log out
        SecurityContextHolder.clearContext();

        // Ensure returns null as no user is logged
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.getLoggedUser();
        });

    }

    @Test
    public void testRegisterUser() {
        // Register new user
        userService.registerUser(
                "asd@asd",
                "password",
                "firstName",
                "lastName");

        // Ensure user exists
        Assertions.assertTrue(userService.existsByEmail("asd@asd"));

        // Ensure error is thrown on registering same email
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.registerUser(
                    "asd@asd",
                    "password",
                    "firstName",
                    "lastName");
        });
    }

    @Test
    public void ensureDefaultUsersAreAddedToRepository() {
        // Arrange / Act
        userService.createDefaultUsers(0);

        // Assert
        Assertions.assertTrue(userRepository.existsByEmail("john@example.com"));
        Assertions.assertTrue(userRepository.existsByEmail("jane@example.com"));
    }

    private void init_testSetProfileImage() {
        User user = userService.registerUser(
                "asd@asd",
                "password",
                "firstName",
                "lastName");
        // Authenticate the user so that setUserProfileImage is able to get the user id from the currently
        // logged-in user.
        user.revokeAuthority("ROLE_UNVERIFIED");
        user.grantAuthority("ROLE_USER");
        userRepository.save(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken("asd@asd",
                "password",
                user.getAuthorities());
        Authentication authenticatedUser = authenticationProvider.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
    }

    @ParameterizedTest
    @MethodSource("validImageTypes")
    public void testSetProfileImage(String fileType, String fileExtension)
            throws IOException, UsernameNotFoundException {
        init_testSetProfileImage();
        MockMultipartFile mockImageFile = new MockMultipartFile("image", "test_pic" + fileExtension,
                fileType, new byte[10]);
        User user = userService.getLoggedUser();
        Long userId = user.getId();
        userService.setUserProfileImage(mockImageFile);
        user = userService.getLoggedUser();

        String filePathString =
                "uploads/profile-images/" + "user_profile_image_id" + userId + fileExtension;
        Path expectedFilePath = Path.of('/' + filePathString);

        Assertions.assertEquals(expectedFilePath, Path.of(user.getProfileImagePath()));
        Assertions.assertEquals(fileType, user.getProfileImageFileType());
        Path imageLocation = Path.of(filePathString.replace("uploads", "test-uploads"));
        Assertions.assertTrue(Files.exists(imageLocation));
        Files.deleteIfExists(imageLocation);
    }

    @Test
    public void givenUserUnverified_whenVerifyUser_thenUserRoleUpdated() {
        User user = new User("asd@asd", passwordEncoder.encode("password"), "firstName",
                "lastName");
        userService.saveUser(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                "password");
        assertThrows(DisabledException.class, () -> SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication)));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        Assertions.assertEquals(1, user.getAuthorities().size());
        Assertions.assertEquals("ROLE_UNVERIFIED",
                user.getAuthorities().get(0).getAuthority());

        userService.verifyUser(user);

        Authentication authentication1 = new UsernamePasswordAuthenticationToken(user.getEmail(),
                "password");
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication1));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        Assertions.assertEquals(1, userService.getLoggedUser().getAuthorities().size());
        Assertions.assertEquals("ROLE_USER",
                userService.getLoggedUser().getAuthorities().get(0).getAuthority());
    }

    @Test
    public void plaintextPassword_resetPassword_passwordHashed() {
        User user = userService.registerUser("john@example.com", "P4$$word", "John", "Doe");

        userService.resetPassword(user, "Hello123*");

        assertNotEquals("Hello123*", user.getPassword());
    }

    @Test
    public void differentPassword_resetPassword_passwordDifferent() {
        User user = userService.registerUser("john@example.com", "P4$$word", "John", "Doe");
        String oldPassword = user.getPassword();

        userService.resetPassword(user, "Hello123*");
        String newPassword = user.getPassword();

        assertNotEquals(oldPassword, newPassword);
    }

    @Test
    public void tokenExists_validateResetPasswordToken_noTokenError() {
        User user = userService.registerUser("john@example.com", "P4$$word", "John", "Doe");
        userService.generateResetPasswordTokenForUser(user);
        String token = user.getResetPasswordToken();
        HashMap<String, String> errors = new HashMap<>();

        userService.validateResetPasswordToken(errors, token);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    public void tokenDoesntExist_validateResetPasswordToken_tokenError() {
        User user = userService.registerUser("john@example.com", "P4$$word", "John", "Doe");
        HashMap<String, String> errors = new HashMap<>();

        userService.validateResetPasswordToken(errors, "token");
        Assertions.assertTrue(errors.containsKey("token"));
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
    }
}

package nz.ac.canterbury.seng302.homehelper.integration.utils;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.dto.UserDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.UserDataValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class UserDataValidatorIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    private static Stream<Arguments> validateEditUser_InvalidData() {
        return Stream.of(
                // First Name Invalid: Empty, Non-letter; Last Name Invalid: Empty, Non-letter; Email Invalid: Invalid format, Empty
                Arguments.of("", "LastName", "test@example.com", List.of("firstName"),
                        List.of("First name cannot be empty")),
                Arguments.of("123", "LastName", "test@example.com", List.of("firstName"),
                        List.of("First name must only include letters, spaces, hyphens or apostrophes")),
                Arguments.of(
                        "FirstNameFirstNameFirstNameFirstNameFirstNameFirstNameFirstNameFirstNameFirstNameFirstNameFirstName",
                        "LastName", "test@example.com", List.of("firstName"),
                        List.of("First name must be 64 characters long or less")),
                Arguments.of("FirstName", "123", "test@example.com", List.of("lastName"),
                        List.of("Last name must only include letters, spaces, hyphens or apostrophes")),
                Arguments.of("FirstName",
                        "LastNameLastNameLastNameLastNameLastNameLastNameLastNameLastNameLastNameLastNameLastName",
                        "test@example.com", List.of("lastName"),
                        List.of("Last name must be 64 characters long or less")),
                Arguments.of("FirstName", "LastName", "", List.of("email"),
                        List.of("Email address must be in the form 'jane@doe.nz'")),
                Arguments.of("123", "", "test@example.com", List.of("firstName"),
                        List.of("First name must only include letters, spaces, hyphens or apostrophes")),
                Arguments.of("FirstName", "", "invalid-email", List.of("email"),
                        List.of("Email address must be in the form 'jane@doe.nz'")),
                Arguments.of("FirstName", "LastName", "invalid-email", List.of("email"),
                        List.of("Email address must be in the form 'jane@doe.nz'")),
                Arguments.of("FirstName", "LastName", "@example.com", List.of("email"),
                        List.of("Email address must be in the form 'jane@doe.nz'")),
                Arguments.of("FirstName", "123", "@example.com", List.of("lastName", "email"),
                        List.of("Last name must only include letters, spaces, hyphens or apostrophes",
                                "Email address must be in the form 'jane@doe.nz'")),
                Arguments.of("123", "123", "@example.com",
                        List.of("firstName", "lastName", "email"),
                        List.of("First name must only include letters, spaces, hyphens or apostrophes",
                                "Last name must only include letters, spaces, hyphens or apostrophes",
                                "Email address must be in the form 'jane@doe.nz'")),
                Arguments.of("", "", "@example.com", List.of("firstName", "email"),
                        List.of("First name cannot be empty",
                                "Email address must be in the form 'jane@doe.nz'")),
                Arguments.of("FirstName", "LastName", "test@.com", List.of("email"),
                        List.of("Email address must be in the form 'jane@doe.nz'")),
                Arguments.of("FirstName", "LastName", "test@com", List.of("email"),
                        List.of("Email address must be in the form 'jane@doe.nz'"))
        );
    }

    private static Stream<Arguments> validateEditPassword_InvalidPasswords() {
        return Stream.of(
                // Generated examples with ChatGPT
                Arguments.of("password", "Pa$$word123", "Pa$$word123", List.of("oldPassword"),
                        List.of("Your old password is incorrect")),
                Arguments.of("P4$$word", "short", "short", List.of("password"), List.of(
                        "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.")),
                Arguments.of("password", "ALLUPPERCASE", "ALLUPPERCASE",
                        List.of("password", "oldPassword"), List.of(
                                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.",
                                "Your old password is incorrect")),
                Arguments.of("P4$$word", "Pa$$word123", "noSpecialChars123", List.of("confirm"),
                        List.of("The new passwords do not match")),
                Arguments.of("password", "password12", "password123",
                        List.of("confirm", "password", "oldPassword"),
                        List.of("The new passwords do not match",
                                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.",
                                "Your old password is incorrect")),
                Arguments.of("P4$$word", "Pa$$word123", "Pa$$word", List.of("confirm"),
                        List.of("The new passwords do not match")),
                Arguments.of("password", "Pa$$word123", "Pa$$word12",
                        List.of("confirm", "oldPassword"),
                        List.of("The new passwords do not match",
                                "Your old password is incorrect")),
                Arguments.of("P4$$word", "short123", "short123", List.of("password"), List.of(
                        "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.")),
                Arguments.of("password", "Password123", "Password123",
                        List.of("password", "oldPassword"), List.of(
                                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.",
                                "Your old password is incorrect")),
                Arguments.of("P4$$word", "Pa$$word123", "Password!@#", List.of("confirm"),
                        List.of("The new passwords do not match")),
                Arguments.of("password", "Pa$$word123", "   ", List.of("confirm", "oldPassword"),
                        List.of("The new passwords do not match", "Your old password is incorrect"))
        );
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void checkOldPasswordMatches_MatchingPassword_ReturnsTrue() {
        userService.registerUser("asd@asd", "password", "firstName", "lastName");

        // Ensure user exists
        Assertions.assertTrue(userService.existsByEmail("asd@asd"));

        Optional<User> user = userService.getUserByEmail("asd@asd");

        HashMap<String, String> errors = new HashMap<>();

        UserDataValidator.validateOldPasswordMatches(errors, passwordEncoder,
                "password", user.get());
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    public void checkOldPasswordMatches_NotMatchingPassword_ReturnsFalse() {
        userService.registerUser("asd@asd", "password", "firstName", "lastName");

        // Ensure user exists
        Assertions.assertTrue(userService.existsByEmail("asd@asd"));

        Optional<User> user = userService.getUserByEmail("asd@asd");

        HashMap<String, String> errors = new HashMap<>();

        UserDataValidator.validateOldPasswordMatches(errors, passwordEncoder,
                "password", user.get());
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    public void validateEditUser_validData_ReturnsNoErrors() {
        String VALID_EMAIL = "john@doe.com";
        String VALID_PASSWORD = "P4$$word";
        String VALID_FIRST_NAME = "firstName";
        String VALID_LAST_NAME = "lastName";
        User user = new User(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), VALID_FIRST_NAME,
                VALID_LAST_NAME);
        userRepository.save(user);
        userService.verifyUser(user);
        String VALID_EMAIL_UPDATED = "john@doe.com";
        String VALID_PASSWORD_UPDATED = "Pa$$word123";
        String VALID_PASSWORD_CONFIRMED = "Pa$$word123";
        String VALID_FIRST_NAME_UPDATED = "Name";
        String VALID_LAST_NAME_UPDATED = "last";
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                VALID_PASSWORD);
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        UserDTO userDTO = new UserDTO(VALID_FIRST_NAME_UPDATED, VALID_LAST_NAME_UPDATED,
                VALID_PASSWORD_UPDATED, VALID_PASSWORD_CONFIRMED, VALID_EMAIL_UPDATED, "", "", "",
                "", "");

        Map<String, String> errors = UserDataValidator.validateEditUser(userDTO, userService);
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    public void validateEditUser_invalidData_ReturnsError() {
        String VALID_EMAIL = "john@doe.com";
        String VALID_PASSWORD = "P4$$word";
        String VALID_FIRST_NAME = "firstName";
        String VALID_LAST_NAME = "lastName";
        User user = new User(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), VALID_FIRST_NAME,
                VALID_LAST_NAME);
        userRepository.save(user);
        userService.verifyUser(user);
        String INVALID_FIRST_NAME_UPDATED = "Name";
        String INVALID_LAST_NAME_UPDATED = "last";
        String INVALID_EMAIL_UPDATED = "john@doe";
        List<String> error_fields = new ArrayList<>();
        List<String> errors_information = new ArrayList<>();
        error_fields.add("email");
        errors_information.add("Email address must be in the form 'jane@doe.nz'");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                VALID_PASSWORD);
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        UserDTO userDTO = new UserDTO(INVALID_FIRST_NAME_UPDATED, INVALID_LAST_NAME_UPDATED, "",
                "", INVALID_EMAIL_UPDATED, "", "", "", "", "");

        Map<String, String> errors = UserDataValidator.validateEditUser(userDTO, userService);
        Assertions.assertEquals(error_fields.size(), errors.size());
        Assertions.assertEquals(errors_information.size(), errors.size());

        int index = 0;
        for (HashMap.Entry<String, String> entry : errors.entrySet()) {
            Assertions.assertEquals(error_fields.get(index), entry.getKey());
            Assertions.assertTrue(entry.getValue().contains(errors_information.get(index)));
            index++;
        }
    }

    @ParameterizedTest
    @MethodSource("validateEditUser_InvalidData")
    public void validateEditUser_invalidDataParameterized_ReturnsError(
            String INVALID_FIRST_NAME_UPDATED, String INVALID_LAST_NAME_UPDATED,
            String INVALID_EMAIL_UPDATED, List<String> error_fields,
            List<String> errors_information) {
        String VALID_EMAIL = "john@doe.com";
        String VALID_PASSWORD = "P4$$word";
        String VALID_FIRST_NAME = "firstName";
        String VALID_LAST_NAME = "lastName";
        User user = new User(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), VALID_FIRST_NAME,
                VALID_LAST_NAME);
        userRepository.save(user);
        userService.verifyUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                VALID_PASSWORD);
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        UserDTO userDTO = new UserDTO(INVALID_FIRST_NAME_UPDATED, INVALID_LAST_NAME_UPDATED, "",
                "", INVALID_EMAIL_UPDATED, "", "", "", "", "");

        Map<String, String> errors = UserDataValidator.validateEditUser(userDTO, userService);
        Assertions.assertEquals(error_fields.size(), errors.size());
        Assertions.assertEquals(errors_information.size(), errors.size());

        int index = 0;
        for (HashMap.Entry<String, String> entry : errors.entrySet()) {
            Assertions.assertEquals(error_fields.get(index), entry.getKey());
            Assertions.assertTrue(entry.getValue().contains(errors_information.get(index)));
            index++;
        }
    }

    @Test
    public void validateEditPassword_validPasswords_ReturnsNoErrors() {
        String VALID_EMAIL = "john@doe.com";
        String VALID_PASSWORD = "P4$$word";
        String VALID_FIRST_NAME = "firstName";
        String VALID_LAST_NAME = "lastName";
        User user = new User(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), VALID_FIRST_NAME,
                VALID_LAST_NAME);
        userRepository.save(user);
        userService.verifyUser(user);
        String VALID_CURRENT_PASSWORD = "P4$$word";
        String VALID_NEW_PASSWORD = "Pa$$word123";
        String VALID_NEW_PASSWORD_CONFIRM = "Pa$$word123";
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                VALID_PASSWORD);
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        Map<String, String> errors = new HashMap<>();

        UserDataValidator.validateEditPassword(errors, passwordEncoder, VALID_CURRENT_PASSWORD,
                VALID_NEW_PASSWORD, VALID_NEW_PASSWORD_CONFIRM, user);
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    public void validateEditPassword_invalidPasswords_ReturnsErrors() {
        String VALID_EMAIL = "john@doe.com";
        String VALID_PASSWORD = "P4$$word";
        String VALID_FIRST_NAME = "firstName";
        String VALID_LAST_NAME = "lastName";
        User user = new User(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), VALID_FIRST_NAME,
                VALID_LAST_NAME);
        userRepository.save(user);
        userService.verifyUser(user);
        String INVALID_CURRENT_PASSWORD = "password";
        String INVALID_NEW_PASSWORD = "Pa$$word123";
        String INVALID_NEW_PASSWORD_CONFIRM = "Pa$$word123";
        List<String> error_fields = new ArrayList<>();
        List<String> errors_information = new ArrayList<>();
        error_fields.add("oldPassword");
        errors_information.add("Your old password is incorrect");

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                VALID_PASSWORD);
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        HashMap<String, String> errors = new HashMap<>();

        UserDataValidator.validateEditPassword(errors,
                passwordEncoder, INVALID_CURRENT_PASSWORD, INVALID_NEW_PASSWORD,
                INVALID_NEW_PASSWORD_CONFIRM, user);
        Assertions.assertEquals(error_fields.size(), errors.size());
        Assertions.assertEquals(errors_information.size(), errors.size());

        int index = 0;
        for (HashMap.Entry<String, String> entry : errors.entrySet()) {
            Assertions.assertEquals(error_fields.get(index), entry.getKey());
            Assertions.assertEquals(errors_information.get(index), entry.getValue());
            index++;
        }
    }

    @ParameterizedTest
    @MethodSource("validateEditPassword_InvalidPasswords")
    public void validateEditPasswordParameterized_invalidPasswords_ReturnsErrors(
            String INVALID_CURRENT_PASSWORD, String INVALID_NEW_PASSWORD,
            String INVALID_NEW_PASSWORD_CONFIRM, List<String> error_fields,
            List<String> errors_information) {
        String VALID_EMAIL = "john@doe.com";
        String VALID_PASSWORD = "P4$$word";
        String VALID_FIRST_NAME = "firstName";
        String VALID_LAST_NAME = "lastName";
        User user = new User(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), VALID_FIRST_NAME,
                VALID_LAST_NAME);
        userRepository.save(user);
        userService.verifyUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),
                VALID_PASSWORD);
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        HashMap<String, String> errors = new HashMap<>();

        UserDataValidator.validateEditPassword(errors,
                passwordEncoder, INVALID_CURRENT_PASSWORD, INVALID_NEW_PASSWORD,
                INVALID_NEW_PASSWORD_CONFIRM, user);
        Assertions.assertEquals(error_fields.size(), errors.size());
        Assertions.assertEquals(errors_information.size(), errors.size());

        int index = 0;
        for (HashMap.Entry<String, String> entry : errors.entrySet()) {
            Assertions.assertEquals(error_fields.get(index), entry.getKey());
            Assertions.assertEquals(errors_information.get(index), entry.getValue());
            index++;
        }
    }
}

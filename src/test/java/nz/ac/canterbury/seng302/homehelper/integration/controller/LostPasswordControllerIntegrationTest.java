package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.controller.LostPasswordController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
public class LostPasswordControllerIntegrationTest {

    @Autowired
    private LostPasswordController Controller;

    @MockBean
    private UserRepository userRepository;

    @SpyBean
    private UserService userService;

    private MockMvc mockMvc;

    private static Stream<Arguments> invalidEmails() {
        return Stream.of(
                Arguments.of("jane@"),
                Arguments.of(""),
                Arguments.of("jane@example"),
                Arguments.of("john@example."),
                Arguments.of("@example.com"),
                Arguments.of("john@example.c"),
                Arguments.of("@")
        );
    }

    private static Stream<Arguments> validEmails() {
        return Stream.of(
                Arguments.of("jane@doe.co.nz"),
                Arguments.of("testEmail@google.com"),
                Arguments.of("jane@example.au"),
                Arguments.of("john.bean@example.nz"),
                Arguments.of("beanFiend@example.com"),
                Arguments.of("beanlover123@example.kz"),
                Arguments.of("आरव@hotmail.xtra"),
                Arguments.of("កំណត់ត្រាជួសជុល1a23@corn.bz")
        );
    }

    private static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of("token", "jane", "jane"),
                Arguments.of("token", "", ""),
                Arguments.of("token", "jane12345", "jane12345"),
                Arguments.of("token", "A1mo#", "A1mo#"),
                Arguments.of("token", "123456789!", "123456789!"),
                Arguments.of("token", "BADBAD", "BADBAD"),
                Arguments.of("token", "wE1$", "wE1$")
        );
    }

    private static Stream<Arguments> validPasswordsNotMatching() {
        return Stream.of(
                Arguments.of("token", "GoodPassword123!", "WOOOoOOO8*"),
                Arguments.of("token", "B3an54lyfe$", ""),
                Arguments.of("token", "jane123#K", "doe123!K"),
                Arguments.of("token", "आरव123Wa$", "B3an54lyfe$")
        );
    }

    private static Stream<Arguments> validPasswordInvalidConfirm() {
        return Stream.of(
                Arguments.of("token", "GoodPassword123!", "*"),
                Arguments.of("token", "B3an54lyfe$", ""),
                Arguments.of("token", "jane123#K", "Badddd1123"),
                Arguments.of("token", "आरव123Wa$", "Ww1@")
        );
    }

    private static Stream<Arguments> invalidPasswordValidConfirm() {
        return Stream.of(
                Arguments.of("token", "*", "GoodPassword123!"),
                Arguments.of("token", "", "B3an54lyfe$"),
                Arguments.of("token", "Baddddd123", "ane123#K"),
                Arguments.of("token", "Ww1@", "आरव123Wa$")
        );
    }

    private static Stream<Arguments> validPasswords() {
        return Stream.of(
                Arguments.of("token", "GoodPassword123!", "GoodPassword123!"),
                Arguments.of("token", "WOOOoOOO8*", "WOOOoOOO8*"),
                Arguments.of("token", "B3an54lyfe$", "B3an54lyfe$"),
                Arguments.of("token", "आरव123Wa$", "आरव123Wa$"),
                Arguments.of("token", "កំណត់ត្រាជួសជុល1aB23@", "កំណត់ត្រាជួសជុល1aB23@")
        );
    }

    private static Stream<Arguments> invalidPasswordsContainsUserDetails() {
        return Stream.of(
                Arguments.of("token", "TestPassword123!", "TestPassword123!"),
                Arguments.of("token", "WOOOoOOO8*test", "WOOOoOOO8*test"),
                Arguments.of("token", "B3an54lyfe$test@example.com", "B3an54lyfe$test@example.com"),
                Arguments.of("token", "आरव12test3Wa$", "आरव12test3Wa$"),
                Arguments.of("token", "កំtest@example.comណត់ត្រាជួសជុល1aB23@",
                        "កំtest@example.comណត់ត្រាជួសជុល1aB23@")
        );
    }

    @PostConstruct
    public void init() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(Controller).build();
        User userWithToken = new User("test@example.com", "Test", "Test", "Test");
        // Set the private field using reflection
        Field field = User.class.getDeclaredField("resetPasswordToken");
        field.setAccessible(true);
        field.set(userWithToken, "token");
        when(userRepository.findByResetPasswordToken("token")).thenReturn(
                Optional.of(userWithToken));
    }

    @ParameterizedTest
    @MethodSource("invalidEmails")
    void testResetPasswordEmailInvalid(String email) throws Exception {
        mockMvc.perform(post("/reset")
                        .param("email", email)
                )
                .andExpect(view().name("lostPassword"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                "email",
                                "Email address must be in the form 'jane@doe.nz'."
                        ))
                );

    }

    @ParameterizedTest
    @MethodSource("validEmails")
    void testResetPasswordEmailValid(String email) throws Exception {
        mockMvc.perform(post("/reset")
                        .param("email", email)
                )
                .andExpect(model().attributeDoesNotExist("errors"))
                .andExpect(view().name("lostPassword"));
    }

    @ParameterizedTest
    @MethodSource("invalidPasswords")
    void testResetPasswordPasswordIsInvalid(String token, String password, String Confirm)
            throws Exception {
        mockMvc.perform(post("/reset")
                        .param("token", String.valueOf(token))
                        .param("password", password)
                        .param("confirm", Confirm)
                )
                .andExpect(model().attributeExists("errors"))
                .andExpect(
                        model().attribute("errors",
                                hasEntry("password", "Your password must be at least " +
                                        "8 characters long and include at least one " +
                                        "uppercase letter, one lowercase letter, one "
                                        + "number, and one special character.")));
        Mockito.verify(userService, Mockito.times(0)).resetPassword(any(User.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("validPasswordsNotMatching")
    void testResetPasswordValidPasswordsNotMatching(String token, String password, String Confirm)
            throws Exception {
        mockMvc.perform(post("/reset")
                        .param("token", String.valueOf(token))
                        .param("password", password)
                        .param("confirm", Confirm)
                )
                .andExpect(model().attributeExists("errors"))
                .andExpect(
                        model().attribute("errors",
                                hasEntry("confirm", "The passwords do not match")));
        Mockito.verify(userService, Mockito.times(0)).resetPassword(any(User.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("validPasswordInvalidConfirm")
    void testResetPasswordValidPasswordInvalidConfirm(String token, String password, String Confirm)
            throws Exception {
        mockMvc.perform(post("/reset")
                        .param("token", String.valueOf(token))
                        .param("password", password)
                        .param("confirm", Confirm)
                )
                .andExpect(model().attributeExists("errors"))
                .andExpect(
                        model().attribute("errors",
                                hasEntry("confirm", "The passwords do not match")));
        Mockito.verify(userService, Mockito.times(0)).resetPassword(any(User.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordValidConfirm")
    void testResetPasswordInvalidPasswordValidConfirm(String token, String password, String Confirm)
            throws Exception {
        mockMvc.perform(post("/reset")
                        .param("token", String.valueOf(token))
                        .param("password", password)
                        .param("confirm", Confirm)
                )
                .andExpect(model().attributeExists("errors"))
                .andExpect(
                        model().attribute("errors",
                                hasEntry("password", "Your password must be at least " +
                                        "8 characters long and include at least one " +
                                        "uppercase letter, one lowercase letter, one "
                                        + "number, and one special character."))
                )
                .andExpect(
                        model().attribute("errors",
                                hasEntry("confirm", "The passwords do not match")));
        Mockito.verify(userService, Mockito.times(0)).resetPassword(any(User.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("validPasswords")
    void testResetPasswordPasswordIsValid(String token, String password, String Confirm)
            throws Exception {
        mockMvc.perform(post("/reset")
                        .param("token", String.valueOf(token))
                        .param("password", password)
                        .param("confirm", Confirm)
                )
                .andExpect(model().attributeDoesNotExist("errors"));
        Mockito.verify(userService, Mockito.times(1)).resetPassword(any(User.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordsContainsUserDetails")
    void testResetPasswordPasswordContainsUserDetails(String token, String password, String Confirm)
            throws Exception {
        mockMvc.perform(post("/reset")
                        .param("token", String.valueOf(token))
                        .param("password", password)
                        .param("confirm", Confirm)
                )
                .andExpect(model().attributeExists("errors"))
                .andExpect(
                        model().attribute("errors",
                                hasEntry("password",
                                        "Your password cannot contain any details from your profile."))
                );
        Mockito.verify(userService, Mockito.times(0)).resetPassword(any(User.class), anyString());
    }

    @Test
    void invalidToken_GetResetPassword_LoginView() throws Exception {
        mockMvc.perform(get("/reset?token=1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void invalidTokenValidPassword_PostResetPassword_LoginView() throws Exception {
        mockMvc.perform(post("/reset?token=1")
                        .param("password", "P4$$word")
                        .param("confirm", "P4$$word"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andExpect(
                        MockMvcResultMatchers.flash()
                                .attribute("error", "Reset password link has expired"));
    }

    @Test
    void invalidTokenValidPassword_PostResetPassword_PasswordNotReset() throws Exception {
        mockMvc.perform(post("/reset?token=1")
                .param("password", "P4$$word")
                .param("confirm", "P4$$word"));

        Mockito.verify(userService, Mockito.times(0)).resetPassword(any(User.class), anyString());
    }
}

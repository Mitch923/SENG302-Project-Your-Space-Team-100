package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.dto.UserDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.SpringEmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class RegistrationControllerIntegrationTest {

    @Autowired
    private RegisterController registerController;

    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SpringEmailService springEmailService;

    // Do not remove this or the tests will fail!
    @MockBean
    private TokenRepository tokenRepository;

    private static Stream<Arguments> validUserData() {
        return Stream.of(
                Arguments.of("John", "Doe", "johndoe@example.com", "P4$$word", "P4$$word"),
                Arguments.of("Jane", "Doe", "janedoe@example.co", "pAsSwOrD1!", "pAsSwOrD1!"),
                // .co in the email is intentional
                Arguments.of("ليلى", "السيد", "abc@def.com", "password5%P", "password5%P"),
                Arguments.of("आरव", "शर्मा", "hello@gmail.com", "ThisIsAPassword?6",
                        "ThisIsAPassword?6"),
                Arguments.of("Person", "", "hi@gmail.com", "!@#$%^&*()Aa1", "!@#$%^&*()Aa1"),
                Arguments.of("José", "González", "jose@jose.com", "he11O!!!", "he11O!!!"),
                Arguments.of("Hōne", "Tāwhai", "hone@xtra.co.nz", "QWERTy9#", "QWERTy9#")
        );
    }

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

    private static Stream<Arguments> validLocationData() {
        return Stream.of(
                Arguments.of("21 Main St", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("1234", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("Main-St", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("/-. '", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("21 Main St", "", "CityTown", "1234", "NZ"),
                Arguments.of("21 Main St", "/-. '", "CityTown", "1234", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "/-. '", "1234", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "1234", "/-. '"),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "123 456", "NZ")
        );
    }

    @PostConstruct
    public void init() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController).build();

        doAnswer(invocation -> {
            User user = (User) invocation.getArguments()[0];
            if (user.getId() == null) {
                // The mock repository doesn't update the id in the user, so make it
                Field field = User.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(user, 1L);
            }
            return null;
        }).when(userRepository).save(any(User.class));
    }

    @Test
    void testGetRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("registerPage"))
                .andExpect(model().attributeExists("userDTO"))
                .andExpect(model().attributeDoesNotExist("errors"))
                .andExpect(model().attribute("userDTO", instanceOf(UserDTO.class)));
    }

    @ParameterizedTest
    @MethodSource("validUserData")
    void testSuccessfulRegistrationWithoutLocation(String firstName, String lastName, String email,
            String password, String confirmedPassword) throws Exception {
        mockMvc.perform(post("/register")
                .param("firstname", firstName)
                .param("lastname", lastName)
                .param("email", email)
                .param("password", password)
                .param("confirm", confirmedPassword)
        );
        ArgumentCaptor<User> userResultCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userResultCaptor.capture());
        User capturedUserResult = userResultCaptor.getValue();
        Assertions.assertNotNull(capturedUserResult);
        Assertions.assertEquals(capturedUserResult.getFirstName(), firstName);
        Assertions.assertEquals(capturedUserResult.getEmail(), email);
        Assertions.assertNotEquals(capturedUserResult.getPassword(), password);
    }

    @ParameterizedTest
    @MethodSource("validLocationData")
    void testSuccessfulRegistrationWithLocation(String street, String suburb, String city,
            String postcode, String country) throws Exception {
        mockMvc.perform(post("/register")
                .param("firstname", "Sarah")
                .param("lastname", "Lee")
                .param("email", "sarah@example.com")
                .param("password", "Password1!")
                .param("confirm", "Password1!")
                .param("streetAddress", street)
                .param("suburb", suburb)
                .param("city", city)
                .param("postcode", postcode)
                .param("country", country)
        );
        ArgumentCaptor<User> userResultCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userResultCaptor.capture());
        List<User> capturedUsers = userResultCaptor.getAllValues();
        Assertions.assertFalse(capturedUsers.isEmpty());
        User capturedUserResult = capturedUsers.get(capturedUsers.size() - 1);
        Location location = capturedUserResult.getUserLocation();
        Assertions.assertNotNull(location);
        Assertions.assertEquals(street, location.getStreet());
        Assertions.assertEquals(suburb, location.getSuburb());
        Assertions.assertEquals(city, location.getCity());
        Assertions.assertEquals(postcode, location.getPostCode());
        Assertions.assertEquals(country, location.getCountry());
    }

    @Test
    void testEmptyFirstName() throws Exception {
        mockMvc.perform(post("/register")
                        .param("firstname", "")
                        .param("lastname", "Doe")
                        .param("email", "email@email.com")
                        .param("password", "StrongPassword123!")
                        .param("confirm", "StrongPassword123!"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                "firstName",
                                "First name cannot be empty"
                        ))
                );
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testInvalidFirstName() throws Exception {
        mockMvc.perform(post("/register")
                        .param("firstname", "Jane$$$")
                        .param("lastname", "Doe")
                        .param("email", "email@email.com")
                        .param("password", "StrongPassword123!")
                        .param("confirm", "StrongPassword123!"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                "firstName",
                                "First name must only include " +
                                        "letters, spaces, hyphens or apostrophes"
                        ))
                );
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testFirstNameTooLong() throws Exception {
        mockMvc.perform(post("/register")
                        .param("firstname", "a".repeat(65))
                        .param("lastname", "Doe")
                        .param("email", "email@email.com")
                        .param("password", "StrongPassword123!")
                        .param("confirm", "StrongPassword123!"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                "firstName",
                                "First name must be 64 characters long or less"
                        ))
                );
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testLastNameTooLong() throws Exception {
        mockMvc.perform(post("/register")
                        .param("firstname", "Jane")
                        .param("lastname", "a".repeat(65))
                        .param("email", "email@email.com")
                        .param("password", "StrongPassword123!")
                        .param("confirm", "StrongPassword123!"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                "lastName",
                                "Last name must be 64 characters long or less"
                        ))
                );
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testLastNameInvalid() throws Exception {
        mockMvc.perform(post("/register")
                        .param("firstname", "Jane")
                        .param("lastname", "Doe$$$")
                        .param("email", "email@email.com")
                        .param("password", "StrongPassword123!")
                        .param("confirm", "StrongPassword123!"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                "lastName",
                                "Last name must only include " +
                                        "letters, spaces, hyphens or apostrophes"
                        ))
                );
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("invalidEmails")
    void testEmailInvalid(String email) throws Exception {
        mockMvc.perform(post("/register")
                        .param("firstname", "Jane")
                        .param("lastname", "Doe")
                        .param("email", email)
                        .param("password", "StrongPassword123!")
                        .param("confirm", "StrongPassword123!"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                is("email"),
                                containsString("Email address must be in the form 'jane@doe.nz'")
                        ))
                );
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testEmailAlreadyExists() throws Exception {
        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);
        mockMvc.perform(post("/register")
                        .param("firstname", "Jane")
                        .param("lastname", "Doe")
                        .param("email", "email@email.com")
                        .param("password", "StrongPassword123!")
                        .param("confirm", "StrongPassword123!"))
                .andExpect(model().attribute("errors", hasEntry(
                        "emailInUse",
                        "This email address is already in use"
                )));
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testPasswordWeak() throws Exception {
        mockMvc.perform(post("/register")
                        .param("firstname", "Jane")
                        .param("lastname", "Doe")
                        .param("email", "email@email.com")
                        .param("password", "weakPassword")
                        .param("confirm", "weakPassword"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                "password",
                                "Your password must be at least " +
                                        "8 characters long and include at least one " +
                                        "uppercase letter, one lowercase letter, one "
                                        + "number, and one special character."
                        ))
                );
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testPasswordNotMatching() throws Exception {
        mockMvc.perform(post("/register")
                        .param("firstname", "Jane")
                        .param("lastname", "Doe")
                        .param("email", "email@email.com")
                        .param("password", "StrongPassword123!")
                        .param("confirm", "NotMatchingPassword123!"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasEntry(
                                "confirm",
                                "The passwords do not match"
                        ))
                );
        verify(springEmailService, times(0))
                .sendSignUpEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }
}

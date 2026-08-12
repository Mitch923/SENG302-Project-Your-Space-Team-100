package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.auth.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.FlashMap;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("unchecked")
public class TagControllerIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private TagService tagService;
    @Autowired
    private RenovationService renovationService;

    @MockBean
    private UserRepository userRepositoryMock;
    @MockBean
    private TagRepository tagRepositoryMock;
    @MockBean
    private RenovationRecordRepository renovationRecordRepositoryMock;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomAuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private RenovationRecord renovationRecord;

    private static Stream<Arguments> validTagNames() {
        return Stream.of(
                Arguments.of("café123"),
                Arguments.of("ï!?"),
                Arguments.of("é#2025"),
                Arguments.of("é"),
                Arguments.of("123straße"),
                Arguments.of("¡fútbol!"),
                Arguments.of("thisisaverycooltag"),
                Arguments.of("hellotowhoeversreadingthesetests"),
                Arguments.of("should tags have spaces?"),
                Arguments.of("1234567890q")
        );
    }

    private static Stream<Arguments> tagNamesNoLetters() {
        return Stream.of(
                Arguments.of("1"),
                Arguments.of("!"),
                Arguments.of(""),
                Arguments.of(""),
                Arguments.of("@"),
                Arguments.of("$"),
                Arguments.of("^"),
                Arguments.of("*"),
                Arguments.of(")"),
                Arguments.of("2"),
                Arguments.of("64"),
                Arguments.of("78"),
                Arguments.of("!@#$"),
                Arguments.of("!23"),
                Arguments.of("103492141*%(#*%#(%)*#%)*#)&%&)#%))@)@)@)@)@!(!)")
        );
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Set up renovation record
        renovationRecord = new RenovationRecord(
                "12 Ilam Road",
                "Repaint and fix the wonky floors");
        Field idField = RenovationRecord.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(renovationRecord, 1L);

        // Use real encoder for consistency
        String rawPassword = "P4$$word";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create verified user
        User john = new User("john@example.com", encodedPassword, "John", "Doe");
        userService.verifyUser(john);
        renovationRecord.setUser(john);

        // Mock repository
        when(tagRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(renovationRecordRepositoryMock.save(any())).thenAnswer(
                invocation -> invocation.getArgument(0));
        when(renovationRecordRepositoryMock.findById(any())).thenReturn(
                Optional.of(renovationRecord));
        when(userRepositoryMock.findByEmailIgnoreCase(any())).thenReturn(Optional.of(john));
        when(userRepositoryMock.findById(any())).thenReturn(Optional.of(john));

        // Authenticate the user
        Authentication authRequest = new UsernamePasswordAuthenticationToken(john.getEmail(),
                rawPassword);
        Authentication authResult = authenticationProvider.authenticate(authRequest);

        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    @ParameterizedTest
    @MethodSource("validTagNames")
    public void validTagName_addTags_noErrorInRedirectAttributes(String tagName) throws Exception {
        FlashMap flashMap = mockMvc.perform(
                        post("/viewRenovation/addTags/" + renovationRecord.getId())
                                .param("tagName", tagName)
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getFlashMap();

        HashMap<String, String> errors = (HashMap<String, String>) flashMap.get("errors");
        assertNull(errors);
    }

    @ParameterizedTest
    @MethodSource("validTagNames")
    public void validTagName5TagsInRenovation_addTags_errorInRedirectAttributes(String tagName)
            throws Exception {
        renovationRecord.addTag(new Tag("tag1"));
        renovationRecord.addTag(new Tag("tag2"));
        renovationRecord.addTag(new Tag("tag1"));
        renovationRecord.addTag(new Tag("tag3"));
        renovationRecord.addTag(new Tag("tag4"));
        renovationRecord.addTag(new Tag("tag5"));

        FlashMap flashMap = mockMvc.perform(
                        post("/viewRenovation/addTags/" + renovationRecord.getId())
                                .param("tagName", tagName)
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getFlashMap();

        HashMap<String, String> errors = (HashMap<String, String>) flashMap.get("errors");
        assertTrue(errors.containsKey("tag"));
        assertEquals("Renovation records cannot have more than 5 tags", errors.get("tag"));
    }

    @ParameterizedTest
    @MethodSource("tagNamesNoLetters")
    public void invalidTagName_addTags_errorInRedirectAttributes(String tagName) throws Exception {
        FlashMap flashMap = mockMvc.perform(
                        post("/viewRenovation/addTags/" + renovationRecord.getId())
                                .param("tagName", tagName)
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getFlashMap();

        HashMap<String, String> errors = (HashMap<String, String>) flashMap.get("errors");
        assertTrue(errors.containsKey("tag"));
        assertEquals("tags must contain letters", errors.get("tag"));
    }

    @ParameterizedTest
    @MethodSource("tagNamesNoLetters")
    public void invalidTagName5TagsInRenovation_addTags_invalidNameErrorInRedirectAttributes(
            String tagName) throws Exception {
        renovationRecord.addTag(new Tag("tag1"));
        renovationRecord.addTag(new Tag("tag2"));
        renovationRecord.addTag(new Tag("tag1"));
        renovationRecord.addTag(new Tag("tag3"));
        renovationRecord.addTag(new Tag("tag4"));
        renovationRecord.addTag(new Tag("tag5"));

        FlashMap flashMap = mockMvc.perform(
                        post("/viewRenovation/addTags/" + renovationRecord.getId())
                                .param("tagName", tagName)
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getFlashMap();

        HashMap<String, String> errors = (HashMap<String, String>) flashMap.get("errors");
        assertTrue(errors.containsKey("tag"));
        assertEquals("tags must contain letters", errors.get("tag"));
    }

    @ParameterizedTest
    @MethodSource("validTagNames")
    public void validTagNameAlreadyInRecord_addTags_errorInRedirectAttributes(String tagName)
            throws Exception {
        renovationRecord.addTag(new Tag(tagName));

        FlashMap flashMap = mockMvc.perform(
                        post("/viewRenovation/addTags/" + renovationRecord.getId())
                                .param("tagName", tagName)
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getFlashMap();

        HashMap<String, String> errors = (HashMap<String, String>) flashMap.get("errors");
        assertTrue(errors.containsKey("tag"));
        assertEquals("Cannot add the same tag to a Renovation Record more than once",
                errors.get("tag"));
    }

    @Test
    public void testAddInvalidTag() throws Exception {
        String newTagName = "  ";
        renovationRecord = renovationService.save(renovationRecord);
        String url = "/viewRenovation/addTags/%d";
        mockMvc.perform(
                        post(String.format(url, renovationRecord.getId())).with(csrf())
                                .param("tagName", newTagName))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        String.format("redirect:/viewRenovation/%d", renovationRecord.getId())));
        Assertions.assertFalse(tagService.existsByName(newTagName));
    }

    @Test
    public void getTags_renovationRecordHasTags_getsAllTags() throws Exception {
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tags.add(new Tag("Tag" + (i + 1)));
        }
        for (Tag tag : tags) {
            renovationRecord.addTag(tag);
        }
        renovationRecord = renovationService.save(renovationRecord);

        String url = "/viewRenovation/%d";
        mockMvc.perform(get(String.format(url, renovationRecord.getId())).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attribute("tags", hasSize(tags.size())));
    }
}

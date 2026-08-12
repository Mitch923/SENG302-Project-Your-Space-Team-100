package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@SuppressWarnings("unchecked")
class RenovationFormControllerIntegrationTest {

    private MockMvc mockMvc;

    private User testUser1;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @MockBean
    private RenovationRecordRepository renovationRecordRepository; // Mock the repository

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Create and authenticate user
        testUser1 = new User("jane@doe.co.nz", passwordEncoder.encode("P4$$word"), "Jane", "Doe");
        testUser1.setId(1L);
        userService.verifyUser(testUser1);
        userRepository.save(testUser1);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "jane@doe.co.nz", "P4$$word"
        );
        Authentication authenticated = authenticationProvider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
    }

    @Test
    void testGetForm() throws Exception {
        mockMvc.perform(get("/createRenovationForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationFormTemplate"));
    }

    @Test
    void testPostForm_validInput_callsRenovationRepository() throws Exception {
        Mockito.when(renovationRecordRepository.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        RenovationRecord renovationRecord = new RenovationRecord("12 Ilam Road",
                "Fixing the undulating floor");
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room("Kitchen", renovationRecord));
        rooms.add(new Room("Bathroom", renovationRecord));
        rooms.add(new Room("Hallway", renovationRecord));
        renovationRecord.addRooms(rooms);
        renovationRecord.setUser(testUser1);
        renovationRecord.setId(1L);
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(renovationRecord));
        String redirectedUrl = mockMvc.perform(post("/createRenovationForm")
                        .param("name", renovationRecord.getName())
                        .param("description", renovationRecord.getDescription())
                        .param("roomNames", "Kitchen", "Bathroom", "Hallway"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse()
                .getRedirectedUrl();
        Assertions.assertEquals("/viewRenovation/null", redirectedUrl);

        mockMvc.perform(get("/viewRenovation/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("record-view"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));

        ArgumentCaptor<RenovationRecord> renovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        Mockito.verify(renovationRecordRepository, Mockito.atLeast(1))
                .save(renovationRecordArgumentCaptor.capture());
        RenovationRecord capturedFormResult = renovationRecordArgumentCaptor.getValue();
        Assertions.assertEquals("12 Ilam Road", capturedFormResult.getName());
        Assertions.assertEquals("Fixing the undulating floor", capturedFormResult.getDescription());
        Assertions.assertEquals(3, capturedFormResult.getRooms().size());
    }

    @Test
    void testPostForm_invalidInput_duplicateRenovationRecord_callsRenovationRepository()
            throws Exception {
        Mockito.when(renovationRecordRepository.findRenovationByName(Mockito.anyString()))
                .thenReturn(new RenovationRecord("name", "description"));
        MvcResult result = mockMvc.perform(post("/createRenovationForm")
                        .with(csrf())
                        .param("name", "12 Ilam Road")
                        .param("description", "Fixing the undulating floor")
                        .param("roomNames", "Kitchen", "Bathroom", "Hallway"))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationFormTemplate"))
                .andReturn();

        HashMap<String, String> errors = (HashMap<String, String>) result.getModelAndView()
                .getModel().get("errors");
        assertTrue(errors.containsKey("duplicate"));
        assertEquals("Renovation record name is not unique", errors.get("duplicate"));
    }

    @Test
    void testPostForm_invalidName_invalidCharacter() throws Exception {
        MvcResult result = mockMvc.perform(post("/createRenovationForm")
                        .with(csrf())
                        .param("name", "Renovation <1>")
                        .param("description", "Fixing the undulating floor")
                        .param("roomNames", "Kitchen", "Bathroom", "Hallway"))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationFormTemplate"))
                .andExpect(model().attribute("name", "Renovation <1>"))
                .andExpect(model().attribute("description", "Fixing the undulating floor"))
                .andExpect(
                        model().attribute("roomNames", List.of("Kitchen", "Bathroom", "Hallway")))
                .andReturn();

        HashMap<String, String> errors = (HashMap<String, String>) result.getModelAndView()
                .getModel().get("errors");
        assertTrue(errors.containsKey("name"));
        assertEquals(
                "Renovation record name must only include letters, numbers, spaces, dots, hyphens or apostrophes",
                errors.get("name"));
    }

    @Test
    void testPostForm_invalidName_emptyName() throws Exception {
        MvcResult result = mockMvc.perform(post("/createRenovationForm")
                        .with(csrf())
                        .param("name", "")
                        .param("description", "Fixing the undulating floor")
                        .param("roomNames", "Kitchen", "Bathroom", "Hallway"))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationFormTemplate"))
                .andExpect(model().attribute("name", ""))
                .andExpect(model().attribute("description", "Fixing the undulating floor"))
                .andExpect(
                        model().attribute("roomNames", List.of("Kitchen", "Bathroom", "Hallway")))
                .andReturn();

        HashMap<String, String> errors = (HashMap<String, String>) result.getModelAndView()
                .getModel().get("errors");
        assertTrue(errors.containsKey("name"));
        assertEquals("Renovation record name cannot be empty", errors.get("name"));
    }

    @Test
    void testPostForm_invalidRooms_invalidCharacter() throws Exception {
        MvcResult result = mockMvc.perform(post("/createRenovationForm")
                        .param("name", "138C clarence street")
                        .param("description", "Fixing the undulating floor")
                        .param("roomNames", "Kitchen_1", "@Bathroom", "#Hallway"))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationFormTemplate"))
                .andExpect(model().attribute("name", "138C clarence street"))
                .andExpect(model().attribute("description", "Fixing the undulating floor"))
                .andExpect(
                        model().attribute("roomNames",
                                List.of("Kitchen_1", "@Bathroom", "#Hallway")))
                .andReturn();

        HashMap<String, String> errors = (HashMap<String, String>) result.getModelAndView()
                .getModel().get("errors");
        assertTrue(errors.containsKey("rooms"));
        assertEquals(
                "Renovation record room names must only include letters, numbers, spaces, dots, hyphens or apostrophes",
                errors.get("rooms"));
    }

    @Test
    void testPostForm_invalidDescription_tooLong() throws Exception {
        String bigString =
                "dffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                        "ffffffffffffffffdfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "fffffffffffffffffffffdffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "ffffffffffffffffffffffffffdfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "fffffffffffffffffffffffffffffffdfffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "fffffffffffffffffffffffffffffffffffdfffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "fffffffffffffffffffffffffffffffffffffff";
        MvcResult result = mockMvc.perform(post("/createRenovationForm")
                        .with(csrf())
                        .param("name", "138C clarence street")
                        .param("description", bigString)
                        .param("roomNames", "Kitchen", "Bathroom", "Hallway"))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationFormTemplate"))
                .andExpect(model().attribute("name", "138C clarence street"))
                .andExpect(model().attribute("description", bigString))
                .andExpect(
                        model().attribute("roomNames", List.of("Kitchen", "Bathroom", "Hallway")))
                .andReturn();

        HashMap<String, String> errors = (HashMap<String, String>) result.getModelAndView()
                .getModel().get("errors");
        assertTrue(errors.containsKey("description"));
        assertEquals("Renovation record description must be 512 characters or less",
                errors.get("description"));
    }

    @Test
    void testPostForm_invalidDescription_tooLong_invalidCharacterInName() throws Exception {
        String bigString =
                "dffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                        "ffffffffffffffffdfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "fffffffffffffffffffffdffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "ffffffffffffffffffffffffffdfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "fffffffffffffffffffffffffffffffdfffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "fffffffffffffffffffffffffffffffffffdfffffffffffffffffffffffffffffffffffffffffffffffffffffff"
                        +
                        "fffffffffffffffffffffffffffffffffffffff";
        MvcResult result = mockMvc.perform(post("/createRenovationForm")
                        .with(csrf())
                        .param("name", "Renovation #1")
                        .param("description", bigString)
                        .param("roomNames", "Kitchen", "Bathroom", "Hallway"))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationFormTemplate"))
                .andExpect(model().attribute("name", "Renovation #1"))
                .andExpect(model().attribute("description", bigString))
                .andExpect(
                        model().attribute("roomNames", List.of("Kitchen", "Bathroom", "Hallway")))
                .andReturn();

        HashMap<String, String> errors = (HashMap<String, String>) result.getModelAndView()
                .getModel().get("errors");
        assertTrue(errors.containsKey("description"));
        assertTrue(errors.containsKey("name"));
        assertEquals("Renovation record description must be 512 characters or less",
                errors.get("description"));
        assertEquals(
                "Renovation record name must only include letters, numbers, spaces, dots, hyphens or apostrophes",
                errors.get("name"));

    }
}
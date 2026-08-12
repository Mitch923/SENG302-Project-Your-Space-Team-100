package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.controller.EditRenovationController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.RoomUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

@SpringBootTest
@SuppressWarnings("unchecked")
public class EditRenovationControllerIntegrationTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    private EditRenovationController editRenovationController;
    private MockMvc mockMvc;
    @Autowired
    private RenovationService renovationService;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    private User testUser1;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static Stream<Arguments> renovationRecordData() {
        return Stream.of(
                Arguments.of("Renovation Record 1", "Renovating Kitchen",
                        List.of("Kitchen", "Dining Room")),
                Arguments.of("Renovation Record 2", "Bathroom upgrade",
                        List.of("Master Bathroom", "Guest Bathroom")),
                Arguments.of("Renovation Record 3", "Adding a new guest bedroom",
                        List.of("Guest Bedroom", "Hallway", "Closet")),
                Arguments.of("Renovation Record 4", "Replacing old flooring",
                        List.of("Living Room", "Hallway", "Dining Room", "Bedroom")),
                Arguments.of("Renovation Record 5", "Complete repainting", List.of("Entire House")),
                Arguments.of("Renovation Record 6", "Roof maintenance", List.of("Roof", "Attic")),
                Arguments.of("Renovation Record 7", "Installing new windows",
                        List.of("Office", "Living Room", "Bedroom", "Dining Room", "Sunroom")),
                Arguments.of("Renovation Record 8", "Expanding backyard patio",
                        List.of("Backyard", "Patio", "Garden", "Outdoor Kitchen")),
                Arguments.of("Renovation Record 9", "Upgrading electrical wiring",
                        List.of("Basement", "Garage", "Kitchen", "Living Room", "Bathroom",
                                "Office")),
                Arguments.of("Renovation Record 10", "Modernizing with smart devices",
                        List.of("Living Room", "Kitchen", "Bedroom", "Office", "Hallway", "Garage"))
        );
    }

    private static Stream<Arguments> renovationRecordDataAndErrors() {
        String massiveDescription = "This is a massive description! ".repeat(100);
        return Stream.of(
                Arguments.of("Yay! I love Renovating!", "Description", "Room1",
                        List.of("Renovation record name must only include letters, numbers, spaces, dots, hyphens or apostrophes")),
                Arguments.of("", "Description", "Room1, Room2",
                        List.of("Renovation record name cannot be empty")),
                Arguments.of("Duplicate Renovation", "Description", "",
                        List.of("Renovation record name is not unique")),
                Arguments.of("Renovation", massiveDescription, "",
                        List.of("Renovation record description must be 512 characters or less")),
                Arguments.of("Duplicate Renovation", massiveDescription, "",
                        List.of("Renovation record description must be 512 characters or less",
                                "Renovation record name is not unique")
                )
        );
    }

    @BeforeEach
    public void before() {
        testUser1 = new User("jane@doe.co.nz", passwordEncoder.encode("P4$$word"), "Jane", "Doe");
        testUser1.setId(1L);
        userService.verifyUser(testUser1);
        userRepository.save(testUser1);
        Authentication authentication = new UsernamePasswordAuthenticationToken("jane@doe.co.nz",
                "P4$$word");
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    }

    @PostConstruct
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(editRenovationController).build();
    }

    public RenovationRecord createRenovationRecord(String name, String description,
            List<String> roomNames, Long userId) {
        RenovationRecord renovationRecord = new RenovationRecord(testUser1, name, description);
        List<Room> rooms = new ArrayList<>();
        Long currentRoomId = 0L;
        for (String roomName : roomNames) {
            if (!roomName.isBlank()) {
                Room room = new Room(roomName, renovationRecord);
                rooms.add(room);
                room.setId(currentRoomId++);
            }
        }
        renovationRecord.setId(userId);
        if (!rooms.isEmpty()) {
            RenovationDesign renovationDesign = new RenovationDesign("Design 1", "A design",
                    renovationRecord);
            rooms.get(0).addDesign(renovationDesign);
            renovationRecord.addDesign(renovationDesign);
        }

        renovationRecord.replaceRooms(rooms);
        return renovationRecord;
    }

    @ParameterizedTest
    @MethodSource("renovationRecordData")
    void getEditRenovationHasModelAttributesParameterisedTest(String name, String description,
            List<String> roomNames) throws Exception {
        RenovationRecord renovationRecord = createRenovationRecord(name, description, roomNames,
                1L);

        Mockito.when(renovationRecordRepository.save(renovationRecord))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(renovationRecordRepository.findById(1L))
                .thenAnswer(invocation -> Optional.of(renovationRecord));
        renovationRecord.setId(1L);
        renovationService.addRenovationRecord(renovationRecord, null);
        mockMvc.perform(get("/editRenovation/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renovation", renovationRecord))
                .andExpect(
                        model().attribute("descriptionLength",
                                renovationRecord.getDescription().length()))
                .andExpect(model().attribute("roomSummaries",
                        RoomUtilities.generateSimplifiedRoomSummaries(renovationRecord.getRooms())))
                .andExpect(view().name("editRenovationForm"));
    }

    @ParameterizedTest
    @MethodSource("renovationRecordData")
    void postEditRenovationEditsRenovationTest(String updatedName, String updatedDescription,
            List<String> roomNames) throws Exception {
        RenovationRecord renovationRecord = createRenovationRecord("Name", "Description", roomNames,
                1L);

        Mockito.when(renovationRecordRepository.save(renovationRecord))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(renovationRecordRepository.findById(1L))
                .thenAnswer(invocation -> Optional.of(renovationRecord));

        renovationService.addRenovationRecord(renovationRecord, null);

        String roomIds = IntStream.range(0, roomNames.size())
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(","));

        mockMvc.perform(post("/editRenovation/1")
                        .param("name", updatedName)
                        .param("description", updatedDescription)
                        .param("roomNames", String.join(",", roomNames))
                        .param("roomIds", roomIds))
                .andExpect(status().isFound()) // Redirect to renovation view
                .andExpect(view().name("redirect:/viewRenovation/" + renovationRecord.getId()));

        ArgumentCaptor<RenovationRecord> captor = ArgumentCaptor.forClass(RenovationRecord.class);
        Mockito.verify(renovationRecordRepository, Mockito.times(3)).save(captor.capture());
        Assertions.assertEquals(updatedName, captor.getAllValues().get(1).getName());
        Assertions.assertEquals(updatedDescription, captor.getAllValues().get(1).getDescription());
        Assertions.assertEquals(1, captor.getAllValues().get(1).getDesignsForRenovation().size());
        Assertions.assertArrayEquals(roomNames.toArray(),
                captor.getAllValues().get(1).getRooms().stream().map(Room::getName).toArray());
    }

    @ParameterizedTest
    @MethodSource("renovationRecordDataAndErrors")
    void postEditRenovationInvalidNameTest(String editedName, String editedDescription,
            String roomNames, List<String> errorMessages) throws Exception {
        RenovationRecord renovationRecord = createRenovationRecord("Name", "Description",
                List.of(roomNames.split(",")), 1L);

        RenovationRecord renovationRecord2 = createRenovationRecord("Duplicate Renovation",
                "Description2", List.of(roomNames.split(",")), 2L);

        Mockito.when(renovationRecordRepository.save(renovationRecord))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(renovationRecordRepository.save(renovationRecord2))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(renovationRecordRepository.findRenovationByName("Duplicate Renovation"))
                .thenReturn(renovationRecord2);
        Mockito.when(renovationRecordRepository.findById(1L))
                .thenReturn(Optional.of(renovationRecord));
        Mockito.when(renovationRecordRepository.findById(2L))
                .thenReturn(Optional.of(renovationRecord2));

        renovationService.addRenovationRecord(renovationRecord, null);
        renovationService.addRenovationRecord(renovationRecord2, null);
        MvcResult result = mockMvc.perform(post("/editRenovation/1")
                        .param("name", editedName)
                        .param("description", editedDescription)
                        .param("roomNames", roomNames))
                .andExpect(status().isOk())
                .andExpect(view().name("editRenovationForm"))
                .andExpect(model().attribute("descriptionLength", editedDescription.length()))
                .andReturn();

        HashMap<String, String> errors = (HashMap<String, String>) result.getModelAndView()
                .getModel().get("errors");
        errorMessages.forEach(errorMessage -> {
            assertTrue(errors.containsValue(errorMessage));
        });

        Mockito.verify(renovationRecordRepository, Mockito.times(2))
                .save(Mockito.any(
                        RenovationRecord.class)); // Expected twice as test renovations saved
    }

    @Test
    public void attemptRoomWithDesign_AttemptDeleteRoom_DeleteFails() throws Exception {
        RenovationRecord renovationRecord = createRenovationRecord("Name", "Description",
                List.of("Room 1", "Room 2"), 1L);

        Mockito.when(renovationRecordRepository.save(renovationRecord))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(renovationRecordRepository.findById(1L))
                .thenReturn(Optional.of(renovationRecord));

        renovationService.addRenovationRecord(renovationRecord, null);

        MvcResult result = mockMvc.perform(post("/editRenovation/1")
                        .param("name", "Name")
                        .param("description", "Description")
                        .param("roomNames", "Room 2"))
                .andExpect(status().isOk())
                .andExpect(view().name("editRenovationForm"))
                .andReturn();

        HashMap<String, String> errors = (HashMap<String, String>) result.getModelAndView()
                .getModel().get("errors");
        assertTrue(errors.containsKey("unmodifiable"));
        assertEquals("Cannot delete rooms that have designs associated with them",
                errors.get("unmodifiable"));
    }
}

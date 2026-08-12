package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.controller.MyRenovationsPageController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
public class RenovationPageControllerIntegrationTest {

    @Autowired
    MyRenovationsPageController myRenovationsPageController;
    @Autowired
    RenovationService renovationService;
    @Autowired
    RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    private MockMvc mockMvc;
    private User testUser1;
    private User testUser2;
    @Autowired
    private UserRepository userRepository;

    private static Stream<Arguments> renovationRecordData() {
        return Stream.of(
                Arguments.of("Renovation Record 1", "Renovating Kitchen",
                        List.of("Kitchen", "Dining Room")),
                Arguments.of("Renovation Record 2", "Bathroom upgrade",
                        List.of("Master Bathroom", "Guest Bathroom")),
                Arguments.of("Renovation Record 3", "Adding a new guest bedroom",
                        List.of("Guest Bedroom", "Hallway", "Closet")));
    }

    @PostConstruct
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(myRenovationsPageController).build();
    }

    @BeforeEach
    public void before() {
        testUser1 = new User("jane@doe.co.nz", passwordEncoder.encode("P4$$word"), "Jane", "Doe");
        testUser2 = new User("adam@smith.nz", passwordEncoder.encode("P4$$word"), "Adam", "Smith");
        testUser1.setId(1L);
        testUser2.setId(2L);
        userRepository.save(testUser1);
        userService.verifyUser(testUser1);
        userRepository.save(testUser2);
        userService.verifyUser(testUser2);
    }

    @AfterEach
    public void tearDown() {
        renovationRecordRepository.deleteAll();
    }

    @Test
    public void testGetRenovationPage_noValues() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "jane@doe.co.nz", "P4$$word"
        );
        Authentication authenticated = authenticationProvider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        mockMvc.perform(get("/myRenovations"))
                .andExpect(status().isOk())
                .andExpect(view().name("myRenovationsTemplate"))
                .andExpect(model().size(1))
                .andExpect(model().attributeExists("renovations"));
    }

    @ParameterizedTest
    @MethodSource("renovationRecordData")
    public void testGetRenovationRecordsWithCorrectUser(String name, String description,
            List<String> roomNames) throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "jane@doe.co.nz", "P4$$word"
        );
        Authentication authenticated = authenticationProvider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        RenovationRecord renovationRecord = new RenovationRecord(name, description);
        renovationRecord.setUser(testUser1);
        List<Room> rooms = new ArrayList<>();
        for (String roomName : roomNames) {
            rooms.add(new Room(roomName, renovationRecord));
        }

        renovationService.addRenovationRecord(renovationRecord, rooms);
        Map<String, Object> model = mockMvc.perform(get("/myRenovations"))
                .andExpect(status().isOk())
                .andExpect(view().name("myRenovationsTemplate"))
                .andReturn().getModelAndView().getModel();

        List<RenovationRecord> renovationRecords = (List<RenovationRecord>) model.get(
                "renovations");
        Assertions.assertNotNull(renovationRecords);
        Assertions.assertEquals(1, renovationRecords.size());
        Assertions.assertEquals(name, renovationRecords.get(0).getName());
        Assertions.assertEquals(description, renovationRecords.get(0).getDescription());
        for (int i = 0; i < rooms.size(); i++) {
            Assertions.assertEquals(rooms.get(i).getName(),
                    renovationRecords.get(0).getRooms().get(i).getName());
        }
    }

    @ParameterizedTest
    @MethodSource("renovationRecordData")
    public void testGetRenovationRecordsWithIncorrectUser(String name, String description,
            List<String> roomNames) throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "adam@smith.nz", "P4$$word"
        );
        Authentication authenticated = authenticationProvider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        RenovationRecord renovationRecord = new RenovationRecord(name, description);
        renovationRecord.setUser(testUser1);
        List<Room> rooms = new ArrayList<>();
        for (String roomName : roomNames) {
            rooms.add(new Room(roomName, renovationRecord));
        }
        renovationService.addRenovationRecord(renovationRecord, rooms);

        Map<String, Object> model = mockMvc.perform(get("/myRenovations"))
                .andExpect(status().isOk())
                .andExpect(view().name("myRenovationsTemplate"))
                .andReturn().getModelAndView().getModel();

        List<RenovationRecord> renovationRecords = (List<RenovationRecord>) model.get(
                "renovations");
        Assertions.assertTrue(renovationRecords.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("renovationRecordData")
    public void testDeleteRenovationRecordWithCorrectUser(String name, String description,
            List<String> roomNames) throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "jane@doe.co.nz", "P4$$word"
        );
        Authentication authenticated = authenticationProvider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        RenovationRecord renovationRecord = new RenovationRecord(testUser1, name, description);
        List<Room> rooms = new ArrayList<>();
        for (String roomName : roomNames) {
            rooms.add(new Room(roomName, renovationRecord));
        }
        long id = renovationService.addRenovationRecord(renovationRecord, rooms).getId();

        mockMvc.perform(post("/deleteRenovation").param("renovationId", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myRenovations"));

        List<RenovationRecord> records = renovationService.getRenovationRecords(testUser1);
        Assertions.assertFalse(records.contains(renovationRecord));
        Assertions.assertEquals(0, records.size());

    }

    @ParameterizedTest
    @MethodSource("renovationRecordData")
    public void testDeleteRenovationRecordWithIncorrectUser(String name, String description,
            List<String> roomNames) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "adam@smith.nz", "P4$$word"
        );
        Authentication authenticated = authenticationProvider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        RenovationRecord renovationRecord = new RenovationRecord(testUser1, name, description);
        List<Room> rooms = new ArrayList<>();
        for (String roomName : roomNames) {
            rooms.add(new Room(roomName, renovationRecord));
        }
        long id = renovationService.addRenovationRecord(renovationRecord, rooms).getId();

        Assertions.assertThrows(ServletException.class, () -> {
            mockMvc.perform(post("/deleteRenovation").param("renovationId", String.valueOf(id)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/myRenovations"));
        });

        List<RenovationRecord> renovationRecords = (renovationService.getRenovationRecords(
                testUser1));
        Assertions.assertNotNull(renovationRecords);
        Assertions.assertEquals(1, renovationRecords.size());
        Assertions.assertEquals(name, renovationRecords.get(0).getName());
        Assertions.assertEquals(description, renovationRecords.get(0).getDescription());
        for (int i = 0; i < rooms.size(); i++) {
            Assertions.assertEquals(rooms.get(i).getName(),
                    renovationRecords.get(0).getRooms().get(i).getName());
        }
    }
}

package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

@SpringBootTest
public class RecordDisplayControllerTest {

    @Autowired
    RenovationDesignService renovationDesignService;

    @MockBean
    RenovationRecordRepository renovationRecordRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @MockBean
    RenovationDesignRepository renovationDesignRepository;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;
    private User testUser1;

    @Autowired
    private WebApplicationContext context;

    private static Stream<Arguments> renovationRecordArguments() {
        return Stream.of(
                Arguments.of("Renovation Record 1", "Renovating Kitchen",
                        List.of("Kitchen", "Dining Room"), "1"),
                Arguments.of("Renovation Record 2", "Bathroom upgrade",
                        List.of("Master Bathroom", "Guest Bathroom"), "2"),
                Arguments.of("Renovation Record 3", "Adding a new guest bedroom",
                        List.of("Guest Bedroom", "Hallway", "Closet"), "3"),
                Arguments.of("Renovation Record 4", "Replacing old flooring",
                        List.of("Living Room", "Hallway", "Dining Room", "Bedroom"), "4"),
                Arguments.of("Renovation Record 5", "Complete repainting", List.of("Entire House"),
                        "5"),
                Arguments.of("Renovation Record 6", "Roof maintenance", List.of("Roof", "Attic"),
                        "6"),
                Arguments.of("Renovation Record 7", "Installing new windows",
                        List.of("Office", "Living Room", "Bedroom", "Dining Room", "Sunroom"), "7"),
                Arguments.of("Renovation Record 8", "Expanding backyard patio",
                        List.of("Backyard", "Patio", "Garden", "Outdoor Kitchen"), "8"),
                Arguments.of("Renovation Record 9", "Upgrading electrical wiring",
                        List.of("Basement", "Garage", "Kitchen", "Living Room", "Bathroom",
                                "Office"), "9"),
                Arguments.of("Renovation Record 10", "Modernizing with smart devices",
                        List.of("Living Room", "Kitchen", "Bedroom", "Office", "Hallway", "Garage"),
                        "10")
        );
    }

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

    @AfterEach
    public void tearDown() {
        renovationRecordRepository.deleteAll();
        userRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("renovationRecordArguments")
    void testGetForm_WithParameters(String name, String description, List<String> roomNames,
            String renovationId) throws Exception {

        RenovationRecord renovationRecord = new RenovationRecord(testUser1, name, description);
        renovationRecord.setId(1L);
        List<Room> rooms = new ArrayList<>();
        for (String roomName : roomNames) {
            rooms.add(new Room(roomName, renovationRecord));
        }
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(renovationRecord));

        MvcResult result = mockMvc.perform(get("/viewRenovation/" + renovationId).with(csrf()))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        RenovationRecord extractedRenovationRecord = (RenovationRecord) modelAndView.getModel()
                .get("renovationRecord");

        Assertions.assertEquals(name, extractedRenovationRecord.getName());
        Assertions.assertEquals(description, extractedRenovationRecord.getDescription());
        for (int i = 0; i < extractedRenovationRecord.getRooms().size(); i++) {
            Assertions.assertEquals(rooms.get(i).getName(),
                    extractedRenovationRecord.getRooms().get(i).getName());
        }
    }


    @Test
    void setVisibility_recordNotPublic_recordMadePublic() throws Exception {

        RenovationRecord renovationRecord = new RenovationRecord("Renovation record",
                "Renovating Jack Erskine");
        renovationRecord.setId(1L);
        renovationRecord.setUser(userService.getLoggedUser());

        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(renovationRecord));
        Mockito.when(renovationRecordRepository.searchByNameOrDescription(null, null, null))
                .thenReturn(new PageImpl<>(List.of(renovationRecord), PageRequest.of(0, 1), 1));
        mockMvc.perform(post("/viewRenovation/1/setVisibility?visibility=true")
                        .with(csrf()))
                .andExpect(status().isOk());

        Assertions.assertTrue(renovationRecord.isPublicRecord());

    }

    @Test
    void setVisibility_recordPublic_recordMadePrivate() throws Exception {

        RenovationRecord renovationRecord = new RenovationRecord("Renovation record",
                "Renovating Jack Erskine");
        renovationRecord.setPublicRecord(true);
        renovationRecord.setId(1L);
        renovationRecord.setUser(userService.getLoggedUser());

        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(renovationRecord));
        Mockito.when(renovationRecordRepository.searchByNameOrDescription(null, null, null))
                .thenReturn(new PageImpl<>(List.of(renovationRecord), PageRequest.of(0, 1), 1));
        mockMvc.perform(post("/viewRenovation/1/setVisibility?visibility=false")
                        .with(csrf()))
                .andExpect(status().isOk());

        Assertions.assertFalse(renovationRecord.isPublicRecord());

    }

    @Test
    void givenValidValues_WhenPostTaskIconChange_theTaskRepositoryCalled() throws Exception {

        RenovationRecord renovationRecord = new RenovationRecord(testUser1, "Name", "Description");
        RenovationDesign renovationDesign = new RenovationDesign("name", "description",
                renovationRecord,
                new Room("room", renovationRecord));
        Mockito.when(renovationDesignService.getDesignById(Mockito.anyLong()))
                .thenReturn(renovationDesign);

        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(renovationRecord));
        // Arrange
        // Act
        mockMvc.perform(
                        post("/viewRenovation/updateIcon/" + 1)
                                .contentType("application/json")
                                .content("{\"iconName\" : \"testIconName\"}")
                )
                // Assert
                .andExpect(status().isOk());

        verify(renovationDesignRepository, times(1)).updateDesignIconNameById(1L, "testIconName");

    }

    @Test
    void createDesign_createsDesign_newDesignCreated() throws Exception {
        RenovationRecord renovationRecord = new RenovationRecord("Renovation record",
                "Renovating Jack Erskine");
        renovationRecord.setId(1L);
        renovationRecord.setUser(userService.getLoggedUser());

        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(renovationRecord));

        Mockito.when(renovationDesignRepository.save(any()))
                .thenAnswer(invocation -> {
                    RenovationDesign design = invocation.getArgument(0);
                    design.setId(1L);
                    return design;
                });

        mockMvc.perform(
                        post("/renovationRecord/{renovationId}/createDesign", renovationRecord.getId())
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        "redirect:/renovationRecord/" + renovationRecord.getId()
                                + "/editDesign/1"));

        ArgumentCaptor<RenovationDesign> designCaptor = ArgumentCaptor.forClass(
                RenovationDesign.class);
        Mockito.verify(renovationDesignRepository).save(designCaptor.capture());

        RenovationDesign savedRenovationDesign = designCaptor.getValue();

        Assertions.assertNotNull(savedRenovationDesign);
        Assertions.assertEquals("Untitled Design", savedRenovationDesign.getName());
        Assertions.assertEquals(renovationRecord, savedRenovationDesign.getRenovationRecord());
    }


}

package nz.ac.canterbury.seng302.homehelper.integration.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
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
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class RenovationRecordIntegrationTest {

    @Autowired
    RenovationRecordRepository renovationRecordRepository;

    @Autowired
    RenovationService renovationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;

    private static Stream<Arguments> validRenovationRecordWithRoomsAndEdit() {
        return Stream.of(
                Arguments.of("Renovation Record 1", "Renovating Kitchen",
                        List.of("Kitchen", "Dining Room"), "Edited Renovation Record #1",
                        "Renovating Patio", List.of("Patio")),
                Arguments.of("Renovation Record 2", "Bathroom upgrade",
                        List.of("Master Bathroom", "Guest Bathroom"), "Edited Renovation Record #2",
                        "Upgrading Master Bathroom", List.of("Master Bathroom")),
                Arguments.of("Renovation Record 3", "Adding a new guest bedroom",
                        List.of("Guest Bedroom", "Hallway", "Closet"),
                        "Edited Renovation Record #3",
                        "Adding a new guest bedroom with walk-in closet",
                        List.of("Guest Bedroom", "Hallway", "Closet")),
                Arguments.of("Renovation Record 4", "Replacing old flooring",
                        List.of("Living Room", "Hallway", "Dining Room", "Bedroom"),
                        "Edited Renovation Record #4", "Replacing old flooring in living areas",
                        List.of("Living Room", "Hallway", "Dining Room")),
                Arguments.of("Renovation Record 5", "Complete repainting", List.of("Entire House"),
                        "Edited Renovation Record #5", "Complete repainting and accent walls",
                        List.of("Entire House", "Living Room", "Bedroom")),
                Arguments.of("Renovation Record 6", "Roof maintenance", List.of("Roof", "Attic"),
                        "Edited Renovation Record #6", "Roof maintenance and insulation",
                        List.of("Roof", "Attic")),
                Arguments.of("Renovation Record 7", "Installing new windows",
                        List.of("Office", "Living Room", "Bedroom", "Dining Room", "Sunroom"),
                        "Edited Renovation Record #7", "Installing energy-efficient windows",
                        List.of("Office", "Living Room", "Bedroom", "Dining Room", "Sunroom")),
                Arguments.of("Renovation Record 8", "Expanding backyard patio",
                        List.of("Backyard", "Patio", "Garden", "Outdoor Kitchen"),
                        "Edited Renovation Record #8", "Expanding backyard patio with gazebo",
                        List.of("Backyard", "Patio", "Garden", "Outdoor Kitchen")),
                Arguments.of("Renovation Record 9", "Upgrading electrical wiring",
                        List.of("Basement", "Garage", "Kitchen", "Living Room", "Bathroom",
                                "Office"),
                        "Edited Renovation Record #9",
                        "Upgrading electrical wiring and adding outlets",
                        List.of("Basement", "Garage", "Kitchen", "Living Room", "Bathroom",
                                "Office")),
                Arguments.of("Renovation Record 10", "Modernizing with smart devices",
                        List.of("Living Room", "Kitchen", "Bedroom", "Office", "Hallway", "Garage"),
                        "Edited Renovation Record #10", "Installing smart lighting and thermostat",
                        List.of("Living Room", "Kitchen", "Bedroom", "Office", "Hallway", "Garage"))
        );
    }

    private static Stream<Arguments> validRenovationRecordsWithRoomNames() {
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

    private static Stream<Arguments> validRenovationRecords() {
        return Stream.of(
                Arguments.of("Renovation Record 1", "Renovating Kitchen"),
                Arguments.of("Renovation Record 2", "Bathroom upgrade with new tiles"),
                Arguments.of("Renovation Record 3", "Adding a new guest bedroom"),
                Arguments.of("Renovation Record 4", "Replacing old flooring in the living room"),
                Arguments.of("Renovation Record 5", "Complete repainting of the house"),
                Arguments.of("Renovation Record 6", "Roof maintenance and repair"),
                Arguments.of("Renovation Record 7", "Installing new windows in the office"),
                Arguments.of("Renovation Record 8", "Expanding the backyard patio"),
                Arguments.of("Renovation Record 9", "Upgrading electrical wiring for safety"),
                Arguments.of("Renovation Record 10", "Modernizing the home with smart devices")
        );
    }

    private static Stream<RenovationRecord> validRenovationRecordsList() {
        return Stream.of(
                new RenovationRecord("Renovation Record 1", "Renovating Kitchen"),
                new RenovationRecord("Renovation Record 2", "Bathroom upgrade with new tiles"),
                new RenovationRecord("Renovation Record 3", "Adding a new guest bedroom"),
                new RenovationRecord("Renovation Record 4",
                        "Replacing old flooring in the living room"),
                new RenovationRecord("Renovation Record 5", "Complete repainting of the house"),
                new RenovationRecord("Renovation Record 6", "Roof maintenance and repair"),
                new RenovationRecord("Renovation Record 7", "Installing new windows in the office"),
                new RenovationRecord("Renovation Record 8", "Expanding the backyard patio"),
                new RenovationRecord("Renovation Record 9",
                        "Upgrading electrical wiring for safety"),
                new RenovationRecord("Renovation Record 10",
                        "Modernizing the home with smart devices")
        );
    }

    private static Stream<Arguments> searchQueryResults() {
        return Stream.of(
                Arguments.of("Renovation Record 1",
                        List.of("Renovation Record 10", "Renovation Record 1"),
                        List.of("Modernizing the home with smart devices", "Renovating Kitchen")),
                Arguments.of("windows", List.of("Renovation Record 7"),
                        List.of("Installing new windows in the office")),
                Arguments.of("floor",
                        List.of("Renovation Record 4"),
                        List.of("Replacing old flooring in the living room")
                ),
                Arguments.of("smart",
                        List.of("Renovation Record 10"),
                        List.of("Modernizing the home with smart devices")
                ),
                Arguments.of("swimming pool",
                        List.of(),
                        List.of()
                ),
                Arguments.of("Renovation",
                        List.of(
                                "Renovation Record 10",
                                "Renovation Record 9",
                                "Renovation Record 8",
                                "Renovation Record 7",
                                "Renovation Record 6",
                                "Renovation Record 5",
                                "Renovation Record 4",
                                "Renovation Record 3",
                                "Renovation Record 2",
                                "Renovation Record 1"
                        ), List.of(
                                "Modernizing the home with smart devices",
                                "Upgrading electrical wiring for safety",
                                "Expanding the backyard patio",
                                "Installing new windows in the office",
                                "Roof maintenance and repair",
                                "Complete repainting of the house",
                                "Replacing old flooring in the living room",
                                "Adding a new guest bedroom",
                                "Bathroom upgrade with new tiles",
                                "Renovating Kitchen"))
        );
    }

    private static Stream<Arguments> searchQueryResultsWithPagination() {
        return Stream.of(
                Arguments.of("kitchen", 1, 5,
                        List.of("Kitchen Remodel"),
                        List.of("Complete kitchen overhaul with new cabinets and lighting")
                ),
                Arguments.of("bathroom", 1, 5,
                        List.of("Bathroom Upgrade"),
                        List.of("Installing modern fixtures and tiling in the main bathroom")
                ),
                Arguments.of("smart", 1, 5,
                        List.of("Smart Home Integration"),
                        List.of("Adding smart locks, thermostats, and lights")
                ),
                Arguments.of("floor", 1, 5,
                        List.of("Living Room Floor Replacement", "Porch Renovation",
                                "Guest Bedroom Addition"),
                        List.of("Replacing carpet with hardwood flooring",
                                "Refinishing the porch flooring and adding railings",
                                "Building an extra guest bedroom on the second floor")
                ),
                Arguments.of("lighting", 1, 5,
                        List.of("Dining Room Lighting Upgrade", "Backyard Expansion",
                                "Kitchen Remodel"),
                        List.of(
                                "Installing a modern chandelier and dimmers",
                                "Extending patio space and adding outdoor lighting",
                                "Complete kitchen overhaul with new cabinets and lighting"
                        )
                ),
                Arguments.of("garage", 1, 5,
                        List.of("Garage Door Replacement"),
                        List.of("Installing an automatic garage door with remote access")
                ),
                Arguments.of("renovation", 2, 5,
                        List.of(),
                        List.of()
                ),
                Arguments.of("window", 1, 5,
                        List.of("Office Window Installation"),
                        List.of("Installing double-glazed windows in the home office")
                ),
                Arguments.of("window", 2, 5,
                        List.of(),
                        List.of()
                ),
                Arguments.of("swimming pool", 1, 5,
                        List.of(),
                        List.of()
                )
        );
    }

    private static Stream<RenovationRecord> validRenovationRecordsWithVariation() {
        return Stream.of(
                new RenovationRecord("Kitchen Remodel",
                        "Complete kitchen overhaul with new cabinets and lighting"),
                new RenovationRecord("Bathroom Upgrade",
                        "Installing modern fixtures and tiling in the main bathroom"),
                new RenovationRecord("Guest Bedroom Addition",
                        "Building an extra guest bedroom on the second floor"),
                new RenovationRecord("Living Room Floor Replacement",
                        "Replacing carpet with hardwood flooring"),
                new RenovationRecord("Full House Repaint",
                        "Painting the interior and exterior of the house"),
                new RenovationRecord("Roof Repair", "Fixing leaks and reinforcing shingles"),
                new RenovationRecord("Office Window Installation",
                        "Installing double-glazed windows in the home office"),
                new RenovationRecord("Backyard Expansion",
                        "Extending patio space and adding outdoor lighting"),
                new RenovationRecord("Electrical Wiring Upgrade",
                        "Rewiring old circuits to meet safety standards"),
                new RenovationRecord("Smart Home Integration",
                        "Adding smart locks, thermostats, and lights"),
                new RenovationRecord("Basement Waterproofing",
                        "Sealing walls and installing sump pump system"),
                new RenovationRecord("Attic Insulation",
                        "Adding eco-friendly insulation to reduce heat loss"),
                new RenovationRecord("Garage Door Replacement",
                        "Installing an automatic garage door with remote access"),
                new RenovationRecord("Porch Renovation",
                        "Refinishing the porch flooring and adding railings"),
                new RenovationRecord("Dining Room Lighting Upgrade",
                        "Installing a modern chandelier and dimmers")
        );
    }

    public static Stream<Arguments> searchQueryPageByIdResults() {
        return Stream.of(
                Arguments.of("", 2, 10),
                Arguments.of("", 2, 1),
                Arguments.of("", 9, 3),
                Arguments.of("Renovation Record 8", 8, 4),
                Arguments.of("Renovation Record 1", 1, 1),
                Arguments.of("Renovation Record", 6, 7)

        );
    }

    @BeforeEach
    public void before() {
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
    @MethodSource("validRenovationRecords")
    public void addRenovationRecordParameterizedTest(String name, String description) {
        RenovationRecord initialRecord = new RenovationRecord(testUser1, name, description);

        Long id = renovationRecordRepository.save(initialRecord).getId();

        RenovationRecord repositoryRecord = renovationRecordRepository.findById(id).get();

        Assertions.assertNotNull(repositoryRecord);
        Assertions.assertEquals(name, repositoryRecord.getName());
        Assertions.assertEquals(description, repositoryRecord.getDescription());
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecordsWithRoomNames")
    public void addRenovationRecordWithRoomsParameterizedTest(String name, String description,
            List<String> roomNames) {

        RenovationRecord initialRenovationRecord = new RenovationRecord(testUser1, name,
                description);

        List<Room> rooms = new ArrayList<>();
        for (String roomName : roomNames) {
            rooms.add(new Room(roomName, initialRenovationRecord));
        }

        Long id = renovationService.addRenovationRecord(initialRenovationRecord, rooms).getId();

        RenovationRecord repositoryRecord = renovationRecordRepository.findById(id).get();

        Assertions.assertNotNull(repositoryRecord);
        Assertions.assertEquals(name, repositoryRecord.getName());
        Assertions.assertEquals(description, repositoryRecord.getDescription());

        Assertions.assertEquals(roomNames.size(), repositoryRecord.getRooms().size());
        for (int i = 0; i < roomNames.size(); i++) {
            Assertions.assertEquals(rooms.get(i).getName(),
                    repositoryRecord.getRooms().get(i).getName());
        }
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecordWithRoomsAndEdit")
    public void editRenovationRecordByIdParameterizedTest(String initialName,
            String initialDescription, List<String> initialRoomNames, String updatedName,
            String updatedDescription, List<String> updatedRoomNames) {
        RenovationRecord initialRenovationRecord = new RenovationRecord(testUser1, initialName,
                initialDescription);

        List<Room> rooms = new ArrayList<>();
        for (String roomName : initialRoomNames) {
            rooms.add(new Room(roomName, initialRenovationRecord));
        }

        RenovationRecord test = renovationService.addRenovationRecord(initialRenovationRecord,
                rooms);
        Long id = test.getId();
        RenovationRecord repositoryRecord = renovationRecordRepository.findById(id).get();

        Assertions.assertNotNull(repositoryRecord);
        Assertions.assertEquals(initialName, repositoryRecord.getName());
        Assertions.assertEquals(initialDescription, repositoryRecord.getDescription());

        Assertions.assertEquals(initialRoomNames.size(), repositoryRecord.getRooms().size());
        for (int i = 0; i < initialRoomNames.size(); i++) {
            Assertions.assertEquals(rooms.get(i).getName(),
                    repositoryRecord.getRooms().get(i).getName());
        }

        RenovationRecord updatedRenovationRecord = renovationRecordRepository.findById(id).get();
        updatedRenovationRecord.setName(updatedName);
        updatedRenovationRecord.setDescription(updatedDescription);
        List<Long> updatedRoomIds = updatedRoomNames.stream().map(room -> (Long) null).toList();
        renovationService.updateRoomsInRenovationRecord(updatedRenovationRecord, updatedRoomNames,
                updatedRoomIds);
        renovationService.save(updatedRenovationRecord);

        RenovationRecord updatedRepositoryRecord = renovationRecordRepository.findById(id).get();

        Assertions.assertNotNull(updatedRepositoryRecord);
        Assertions.assertEquals(updatedName, updatedRepositoryRecord.getName());
        Assertions.assertEquals(updatedDescription, updatedRepositoryRecord.getDescription());

        Assertions.assertEquals(updatedRoomNames.size(), updatedRepositoryRecord.getRooms().size());
        for (int i = 0; i < updatedRoomNames.size(); i++) {
            Assertions.assertEquals(updatedRoomNames.get(i),
                    updatedRepositoryRecord.getRooms().get(i).getName());
        }
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecords")
    public void deleteRenovationRecordById_ParamaterizedTest(String name, String description) {
        RenovationRecord renovationRecord = new RenovationRecord(testUser1, name, description);
        Long id = renovationService.addRenovationRecord(renovationRecord, null).getId();
        RenovationRecord retrievedRenovationRecord = renovationService.getRenovationRecordById(id);

        // Check the renovation record is present
        Assertions.assertNotNull(retrievedRenovationRecord);
        Assertions.assertEquals(name, retrievedRenovationRecord.getName());
        Assertions.assertEquals(description, retrievedRenovationRecord.getDescription());

        // Delete the record
        renovationService.deleteRenovationRecordById(id);

        // Check the renovation record is no longer there
        Assertions.assertNull(renovationService.getRenovationRecordById(id));
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecordsWithRoomNames")
    public void deleteRenovationRecordById_withRooms_ParamaterizedTest(String name,
            String description, List<String> roomNames) {
        RenovationRecord renovationRecord = new RenovationRecord(testUser1, name, description);
        List<Room> rooms = new ArrayList<>();
        for (String roomName : roomNames) {
            rooms.add(new Room(roomName, renovationRecord));
        }
        Long id = renovationService.addRenovationRecord(renovationRecord, rooms).getId();
        RenovationRecord retrievedRenovationRecord = renovationService.getRenovationRecordById(id);

        // Check the renovation record is present
        Assertions.assertNotNull(retrievedRenovationRecord);
        Assertions.assertEquals(name, retrievedRenovationRecord.getName());
        Assertions.assertEquals(description, retrievedRenovationRecord.getDescription());

        // Delete the record
        renovationService.deleteRenovationRecordById(id);

        // Check the renovation record is no longer there
        Assertions.assertNull(renovationService.getRenovationRecordById(id));
    }

    @ParameterizedTest
    @MethodSource("searchQueryResults")
    public void searchQueryReturnsValidResults(String query, List<String> expectedNames,
            List<String> expectedDescriptions) {
        validRenovationRecordsList().forEach(renovationRecord -> {
            renovationRecord.setUser(testUser1);
            renovationService.addRenovationRecord(renovationRecord, null);
        });
        List<RenovationRecord> searchedRenovation = renovationService.searchRenovationRecords(query,
                testUser1, 10, 1).getContent();

        List<String> expectedNamesSorted = expectedNames.stream().sorted().toList();
        List<RenovationRecord> searchedRenovationSortedByName = searchedRenovation.stream()
                .sorted(Comparator.comparing(RenovationRecord::getName)).toList();
        List<RenovationRecord> searchedRenovationSortedByDesc = searchedRenovation.stream()
                .sorted(Comparator.comparing(RenovationRecord::getDescription)).toList();
        List<String> expectedDescriptionsSorted = expectedDescriptions.stream().sorted().toList();

        Assertions.assertEquals(expectedNames.size(), searchedRenovation.size());

        for (int i = 0; i < searchedRenovation.size(); i++) {
            Assertions.assertEquals(expectedNamesSorted.get(i),
                    searchedRenovationSortedByName.get(i).getName());
            Assertions.assertEquals(expectedDescriptionsSorted.get(i),
                    searchedRenovationSortedByDesc.get(i).getDescription());
        }

    }

    @ParameterizedTest
    @MethodSource("searchQueryPageByIdResults")
    public void searchQueryCheckPageByIdReturnsCorrectPage(String query, long id, int pageSize) {

        validRenovationRecordsList().forEach(renovationRecord -> {
            renovationRecord.setUser(testUser1);
            renovationService.addRenovationRecord(renovationRecord, null);
        });
        long offset = renovationService.getRenovationRecords(testUser1).getFirst().getId();
        long actualId = id + offset - 1;
        int actualPage = renovationService.findPageNumberOfId(actualId, pageSize, query, testUser1);
        List<RenovationRecord> records = renovationService.searchRenovationRecords(query, testUser1,
                pageSize, actualPage).getContent();
        assert records.stream().anyMatch(renovationRecord -> renovationRecord.getId() == actualId);
    }

    @ParameterizedTest
    @MethodSource("searchQueryResultsWithPagination")
    public void searchQueryReturnsValidPagination(String query, int pageNum, int pageSize,
            List<String> expectedNames, List<String> expectedDescriptions) {
        validRenovationRecordsWithVariation().forEach(renovationRecord -> {
            renovationRecord.setUser(testUser1);
            renovationService.addRenovationRecord(renovationRecord, null);
        });
        List<RenovationRecord> searchedRenovation = renovationService.searchRenovationRecords(query,
                testUser1, pageSize, pageNum).getContent();

        Assertions.assertEquals(expectedNames.size(), searchedRenovation.size());

        List<String> expectedNamesSorted = expectedNames.stream().sorted().toList();
        List<RenovationRecord> searchedRenovationSortedByName = searchedRenovation.stream()
                .sorted(Comparator.comparing(RenovationRecord::getName)).toList();
        List<RenovationRecord> searchedRenovationSortedByDesc = searchedRenovation.stream()
                .sorted(Comparator.comparing(RenovationRecord::getDescription)).toList();
        List<String> expectedDescriptionsSorted = expectedDescriptions.stream().sorted().toList();

        for (int i = 0; i < searchedRenovationSortedByName.size(); i++) {
            Assertions.assertEquals(expectedNamesSorted.get(i),
                    searchedRenovationSortedByName.get(i).getName());
            Assertions.assertEquals(expectedDescriptionsSorted.get(i),
                    searchedRenovationSortedByDesc.get(i).getDescription());
        }

    }

    @ParameterizedTest
    @MethodSource("searchQueryResults")
    public void publicSearchQueryReturnsValidResults(String query, List<String> expectedNames,
            List<String> expectedDescriptions) {
        validRenovationRecordsList().forEach(renovationRecord -> {
            renovationRecord.setUser(testUser1);
            renovationRecord.setPublicRecord(true);
            renovationService.addRenovationRecord(renovationRecord, null);
        });
        List<RenovationRecord> searchedRenovation = renovationService.searchRenovationRecords(query,
                null, 10, 1).getContent();

        List<String> expectedNamesSorted = expectedNames.stream().sorted().toList();
        List<RenovationRecord> searchedRenovationSortedByName = searchedRenovation.stream()
                .sorted(Comparator.comparing(RenovationRecord::getName)).toList();
        List<RenovationRecord> searchedRenovationSortedByDesc = searchedRenovation.stream()
                .sorted(Comparator.comparing(RenovationRecord::getDescription)).toList();
        List<String> expectedDescriptionsSorted = expectedDescriptions.stream().sorted().toList();

        Assertions.assertEquals(expectedNames.size(), searchedRenovation.size());

        for (int i = 0; i < searchedRenovation.size(); i++) {
            Assertions.assertEquals(expectedNamesSorted.get(i),
                    searchedRenovationSortedByName.get(i).getName());
            Assertions.assertEquals(expectedDescriptionsSorted.get(i),
                    searchedRenovationSortedByDesc.get(i).getDescription());
        }

    }

    @Test
    public void getPublicRenovations_PublicRecord_returnsRecords() {
        List<RenovationRecord> storedRenovationRecords = new ArrayList<>();
        storedRenovationRecords.add(new RenovationRecord("Renovation record 1", "Description1"));
        storedRenovationRecords.add(new RenovationRecord("Renovation record 2", "Description2"));
        storedRenovationRecords.add(new RenovationRecord("Renovation record 3", "Description3"));

        storedRenovationRecords.getFirst().setPublicRecord(true);
        storedRenovationRecords.getLast().setPublicRecord(true);

        for (RenovationRecord record : storedRenovationRecords) {
            record.setUser(testUser1);
            renovationService.addRenovationRecord(record, null);
        }

        List<RenovationRecord> receivedRecords = renovationService.searchRenovationRecords("", null,
                30, 1).getContent();

        Assertions.assertEquals(storedRenovationRecords.getLast().getName(),
                receivedRecords.getFirst().getName());
        Assertions.assertEquals(storedRenovationRecords.getFirst().getName(),
                receivedRecords.getLast().getName());
    }
}

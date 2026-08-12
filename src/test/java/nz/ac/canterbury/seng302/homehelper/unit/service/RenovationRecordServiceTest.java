package nz.ac.canterbury.seng302.homehelper.unit.service;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.SceneTextureService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class RenovationRecordServiceTest {

    @Mock
    TagService tagService;
    @Mock
    RenovationRecordRepository renovationRecordRepository;
    @Mock
    RoomRepository roomRepository;
    @Mock
    TagRepository tagRepository;
    @Mock
    SceneModelService sceneModelService;
    @Mock
    SceneTextureService sceneTextureService;
    @Mock
    UserService userService;

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

    private static Stream<Arguments> validRenovationRecordIdsAndRecords()
            throws NoSuchFieldException, IllegalAccessException {
        List<RenovationRecord> renovationRecords = List.of(
                new RenovationRecord("Renovation Record 1", "Renovating Kitchen"),
                new RenovationRecord("Renovation Record 2", "Bathroom upgrade"),
                new RenovationRecord("Renovation Record 3", "Adding a new guest bedroom"),
                new RenovationRecord("Renovation Record 4", "Replacing old flooring"),
                new RenovationRecord("Renovation Record 5", "Complete repainting"),
                new RenovationRecord("Renovation Record 6", "Roof maintenance"),
                new RenovationRecord("Renovation Record 7", "Installing new windows"),
                new RenovationRecord("Renovation Record 8", "Expanding backyard patio"),
                new RenovationRecord("Renovation Record 9", "Upgrading electrical wiring"),
                new RenovationRecord("Renovation Record 10", "Modernizing with smart devices")
        );

        renovationRecords.getFirst()
                .addRooms(List.of(new Room("Room1", renovationRecords.getFirst())));
        renovationRecords.get(4).addRooms(List.of(new Room("Room2", renovationRecords.get(4)),
                new Room("Room3", renovationRecords.get(4))));

        Field id = RenovationRecord.class.getDeclaredField("id");
        for (int i = 0; i < renovationRecords.size(); i++) {
            id.setAccessible(true);
            id.set(renovationRecords.get(i), (long) i);
        }

        return Stream.of(
                Arguments.of(0, renovationRecords),
                Arguments.of(1, renovationRecords),
                Arguments.of(2, renovationRecords),
                Arguments.of(3, renovationRecords),
                Arguments.of(4, renovationRecords),
                Arguments.of(5, renovationRecords),
                Arguments.of(6, renovationRecords),
                Arguments.of(7, renovationRecords),
                Arguments.of(8, renovationRecords),
                Arguments.of(9, renovationRecords)
        );
    }

    private static Stream<Arguments> renovationRoomNames() {
        List<RenovationRecord> renovationRecords = List.of(
                new RenovationRecord("Renovation Record 1", "Renovating Kitchen"),
                new RenovationRecord("Renovation Record 2", "Bathroom upgrade"),
                new RenovationRecord("Renovation Record 3", "Adding a new guest bedroom"),
                new RenovationRecord("Renovation Record 4", "Replacing old flooring"),
                new RenovationRecord("Renovation Record 5", "Complete repainting"),
                new RenovationRecord("Renovation Record 6", "Roof maintenance"),
                new RenovationRecord("Renovation Record 7", "Installing new windows"),
                new RenovationRecord("Renovation Record 8", "Expanding backyard patio"),
                new RenovationRecord("Renovation Record 9", "Upgrading electrical wiring"),
                new RenovationRecord("Renovation Record 10", "Modernizing with smart devices")
        );

        renovationRecords.getFirst()
                .addRooms(List.of(new Room("Room1", renovationRecords.getFirst())));
        renovationRecords.get(4).addRooms(List.of(new Room("Room2", renovationRecords.get(4)),
                new Room("Room3", renovationRecords.get(4))));

        return Stream.of(
                Arguments.of("Renovation Record 1", renovationRecords, 0),
                Arguments.of("Renovation Record 2", renovationRecords, 1),
                Arguments.of("Renovation Record 3", renovationRecords, 2),
                Arguments.of("Renovation Record 4", renovationRecords, 3),
                Arguments.of("Renovation Record 5", renovationRecords, 4),
                Arguments.of("Renovation Record 6", renovationRecords, 5)
        );
    }

    private static Stream<Arguments> validRenovationRecordWithRoomsAndEdit() {
        return Stream.of(
                Arguments.of("Renovation Record 1", "Renovating Kitchen",
                        List.of("Kitchen", "Dining Room"), "Edited Renovation Record 1",
                        "Renovating Patio",
                        List.of("Patio")),
                Arguments.of("Renovation Record 2", "Bathroom upgrade",
                        List.of("Master Bathroom", "Guest Bathroom"), "Edited Renovation Record 2",
                        "Upgrading Master Bathroom", List.of("Master Bathroom")),
                Arguments.of("Renovation Record 3", "Adding a new guest bedroom",
                        List.of("Guest Bedroom", "Hallway", "Closet"), "Edited Renovation Record 3",
                        "Adding a new guest bedroom with walk-in closet",
                        List.of("Guest Bedroom", "Hallway", "Closet")),
                Arguments.of("Renovation Record 4", "Replacing old flooring",
                        List.of("Living Room", "Hallway", "Dining Room", "Bedroom"),
                        "Edited Renovation Record 4", "Replacing old flooring in living areas",
                        List.of("Living Room", "Hallway", "Dining Room")),
                Arguments.of("Renovation Record 5", "Complete repainting", List.of("Entire House"),
                        "Edited Renovation Record 5", "Complete repainting and accent walls",
                        List.of("Entire House", "Living Room", "Bedroom")),
                Arguments.of("Renovation Record 6", "Roof maintenance", List.of("Roof", "Attic"),
                        "Edited Renovation Record 6", "Roof maintenance and insulation",
                        List.of("Roof", "Attic")),
                Arguments.of("Renovation Record 7", "Installing new windows",
                        List.of("Office", "Living Room", "Bedroom", "Dining Room", "Sunroom"),
                        "Edited Renovation Record 7", "Installing energy-efficient windows",
                        List.of("Office", "Living Room", "Bedroom", "Dining Room", "Sunroom")),
                Arguments.of("Renovation Record 8", "Expanding backyard patio",
                        List.of("Backyard", "Patio", "Garden", "Outdoor Kitchen"),
                        "Edited Renovation Record 8", "Expanding backyard patio with gazebo",
                        List.of("Backyard", "Patio", "Garden", "Outdoor Kitchen")),
                Arguments.of("Renovation Record 9", "Upgrading electrical wiring",
                        List.of("Basement", "Garage", "Kitchen", "Living Room", "Bathroom",
                                "Office"),
                        "Edited Renovation Record 9",
                        "Upgrading electrical wiring and adding outlets",
                        List.of("Basement", "Garage", "Kitchen", "Living Room", "Bathroom",
                                "Office")),
                Arguments.of("Renovation Record 10", "Modernizing with smart devices",
                        List.of("Living Room", "Kitchen", "Bedroom", "Office", "Hallway", "Garage"),
                        "Edited Renovation Record 10", "Installing smart lighting and thermostat",
                        List.of("Living Room", "Kitchen", "Bedroom", "Office", "Hallway", "Garage"))
        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addRenovationRecordTest() {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        Mockito.doAnswer(invocation -> invocation.getArgument(0))
                .when(renovationRecordRepositorySpy).save(Mockito.any(RenovationRecord.class));

        RenovationService renovationServiceSpy = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);
        renovationServiceSpy.addRenovationRecord(
                new RenovationRecord("Renovation Record 1", "Renovating Kitchen"), null);

        ArgumentCaptor<RenovationRecord> renovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        verify(renovationRecordRepositorySpy)
                .save(renovationRecordArgumentCaptor.capture());

        RenovationRecord capturedRenovationRecord = renovationRecordArgumentCaptor.getValue();
        assertNotNull(capturedRenovationRecord);
        Assertions.assertEquals("Renovation Record 1", capturedRenovationRecord.getName());
        Assertions.assertEquals("Renovating Kitchen", capturedRenovationRecord.getDescription());
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecords")
    public void addRenovationRecordParameterizedTest(String name, String description) {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        Mockito.doAnswer(invocation -> invocation.getArgument(0))
                .when(renovationRecordRepositorySpy).save(Mockito.any(RenovationRecord.class));

        RenovationService renovationServiceSpy = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);
        renovationServiceSpy.addRenovationRecord(new RenovationRecord(name, description), null);

        ArgumentCaptor<RenovationRecord> renovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        verify(renovationRecordRepositorySpy)
                .save(renovationRecordArgumentCaptor.capture());

        RenovationRecord capturedRenovationRecord = renovationRecordArgumentCaptor.getValue();
        assertNotNull(capturedRenovationRecord);
        Assertions.assertEquals(name, capturedRenovationRecord.getName());
        Assertions.assertEquals(description, capturedRenovationRecord.getDescription());
    }

    @Test
    public void addRenovationRecordWithRoomsTest() {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        Mockito.doAnswer(invocation -> invocation.getArgument(0))
                .when(renovationRecordRepositorySpy).save(Mockito.any(RenovationRecord.class));

        RenovationService renovationServiceSpy = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);
        RenovationRecord renovationRecord = new RenovationRecord("Renovation Record 1",
                "Renovating Kitchen");
        Room room = new Room("Kitchen", renovationRecord);
        List<Room> rooms = new ArrayList<>();
        rooms.add(room);
        renovationServiceSpy.addRenovationRecord(renovationRecord, rooms);

        ArgumentCaptor<RenovationRecord> renovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        verify(renovationRecordRepositorySpy, Mockito.atLeast(2))
                .save(renovationRecordArgumentCaptor.capture());

        RenovationRecord capturedRenovationRecord = renovationRecordArgumentCaptor.getValue();
        assertNotNull(capturedRenovationRecord);
        Assertions.assertEquals("Renovation Record 1", capturedRenovationRecord.getName());
        Assertions.assertEquals("Renovating Kitchen", capturedRenovationRecord.getDescription());

        Assertions.assertEquals(1, capturedRenovationRecord.getRooms().size());
        Assertions.assertEquals(room, capturedRenovationRecord.getRooms().get(0));
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecordsWithRoomNames")
    public void addRenovationRecordWithRoomsParameterizedTest(String name, String description,
            List<String> roomNames) {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        Mockito.doAnswer(invocation -> invocation.getArgument(0))
                .when(renovationRecordRepositorySpy).save(Mockito.any(RenovationRecord.class));

        RenovationService renovationServiceSpy = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);
        RenovationRecord renovationRecord = new RenovationRecord(name, description);

        List<Room> rooms = new ArrayList<>();
        for (String roomName : roomNames) {
            rooms.add(new Room(roomName, renovationRecord));
        }

        renovationServiceSpy.addRenovationRecord(renovationRecord, rooms);

        ArgumentCaptor<RenovationRecord> renovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        verify(renovationRecordRepositorySpy, Mockito.atLeast(2))
                .save(renovationRecordArgumentCaptor.capture());

        RenovationRecord capturedRenovationRecord = renovationRecordArgumentCaptor.getValue();
        assertNotNull(capturedRenovationRecord);
        Assertions.assertEquals(name, capturedRenovationRecord.getName());
        Assertions.assertEquals(description, capturedRenovationRecord.getDescription());

        Assertions.assertEquals(roomNames.size(), capturedRenovationRecord.getRooms().size());
        for (int i = 0; i < roomNames.size(); i++) {
            Assertions.assertEquals(rooms.get(i), capturedRenovationRecord.getRooms().get(i));
        }

    }

    @ParameterizedTest
    @MethodSource("validRenovationRecordIdsAndRecords")
    public void getRenovationRecordByIdParameterizedTest(long id,
            List<RenovationRecord> renovationRecords) {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);

        when(renovationRecordRepositorySpy.findById(id))
                .thenReturn(Optional.ofNullable(renovationRecords.get((int) id)));

        RenovationService renovationService = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);
        RenovationRecord receivedRecord = renovationService.getRenovationRecordById(id);

        Assertions.assertEquals(receivedRecord.getName(),
                renovationRecords.get((int) id).getName());
        Assertions.assertEquals(receivedRecord.getDescription(),
                renovationRecords.get((int) id).getDescription());
        Assertions.assertArrayEquals(receivedRecord.getRooms().toArray(),
                renovationRecords.get((int) id).getRooms().toArray());
    }

    @ParameterizedTest
    @MethodSource("renovationRoomNames")
    public void getRenovationRecordByNameTest(String name, List<RenovationRecord> renovationRecords,
            int recordIndex) {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        when(renovationRecordRepositorySpy.findRenovationByName(name))
                .thenReturn(renovationRecords.get(recordIndex));

        RenovationService renovationServiceSpy = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);

        RenovationRecord receivedRecord = renovationServiceSpy.getRenovationRecordByName(name);

        Assertions.assertEquals(receivedRecord.getName(), name);
        Assertions.assertEquals(receivedRecord.getDescription(),
                renovationRecords.get(recordIndex).getDescription());
        Assertions.assertArrayEquals(receivedRecord.getRooms().toArray(),
                renovationRecords.get(recordIndex).getRooms().toArray());
    }

    @Test
    public void editRenovationRecordByIdTest() {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        Mockito.doAnswer(invocation -> invocation.getArgument(0))
                .when(renovationRecordRepositorySpy).save(Mockito.any(RenovationRecord.class));

        RenovationService renovationServiceSpy = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);
        RenovationRecord renovationRecord = new RenovationRecord("Renovation Record 1",
                "Renovating Kitchen");
        Room room = new Room("Kitchen", renovationRecord);
        List<Room> rooms = new ArrayList<>();
        rooms.add(room);
        renovationServiceSpy.addRenovationRecord(renovationRecord, rooms);

        ArgumentCaptor<RenovationRecord> renovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        verify(renovationRecordRepositorySpy, Mockito.atLeast(2))
                .save(renovationRecordArgumentCaptor.capture());

        RenovationRecord capturedRenovationRecord = renovationRecordArgumentCaptor.getValue();
        assertNotNull(capturedRenovationRecord);
        Assertions.assertEquals("Renovation Record 1", capturedRenovationRecord.getName());
        Assertions.assertEquals("Renovating Kitchen", capturedRenovationRecord.getDescription());

        Mockito.doAnswer(invocationOnMock -> Optional.of(renovationRecord))
                .when(renovationRecordRepositorySpy).findById(anyLong());

        Assertions.assertEquals(1, capturedRenovationRecord.getRooms().size());
        Assertions.assertEquals(room, capturedRenovationRecord.getRooms().get(0));

        RenovationRecord updatedRenovationRecord = new RenovationRecord("Renovation Record 2",
                "Renovating Bathroom");
        Room updatedRoom = new Room("Bathroom", updatedRenovationRecord);
        List<Room> updatedRooms = new ArrayList<>();
        updatedRooms.add(updatedRoom);
        updatedRenovationRecord.addRooms(updatedRooms);
        renovationServiceSpy.save(updatedRenovationRecord);

        ArgumentCaptor<RenovationRecord> updatedRenovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        verify(renovationRecordRepositorySpy, Mockito.atLeast(2))
                .save(updatedRenovationRecordArgumentCaptor.capture());

        RenovationRecord captureUpdatedRenovationRecord = updatedRenovationRecordArgumentCaptor.getValue();
        assertNotNull(captureUpdatedRenovationRecord);
        Assertions.assertEquals("Renovation Record 2", captureUpdatedRenovationRecord.getName());
        Assertions.assertEquals("Renovating Bathroom",
                captureUpdatedRenovationRecord.getDescription());

        Assertions.assertEquals(1, captureUpdatedRenovationRecord.getRooms().size());
        Assertions.assertEquals(updatedRoom.getName(),
                captureUpdatedRenovationRecord.getRooms().get(0).getName());
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecordWithRoomsAndEdit")
    public void editRenovationRecordByIdParameterizedTest(String initialName,
            String initialDescription, List<String> initialRoomNames, String updatedName,
            String updatedDescription, List<String> updatedRoomNames) {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        Mockito.doAnswer(invocation -> invocation.getArgument(0))
                .when(renovationRecordRepositorySpy).save(Mockito.any(RenovationRecord.class));

        RenovationService renovationServiceSpy = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);
        RenovationRecord renovationRecord = new RenovationRecord(initialName, initialDescription);
        List<Room> initialRooms = new ArrayList<>();
        for (String roomName : initialRoomNames) {
            initialRooms.add(new Room(roomName, renovationRecord));
        }
        renovationServiceSpy.addRenovationRecord(renovationRecord, initialRooms);

        ArgumentCaptor<RenovationRecord> renovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        verify(renovationRecordRepositorySpy, Mockito.atLeast(2))
                .save(renovationRecordArgumentCaptor.capture());

        RenovationRecord capturedRenovationRecord = renovationRecordArgumentCaptor.getValue();
        assertNotNull(capturedRenovationRecord);
        Assertions.assertEquals(initialName, capturedRenovationRecord.getName());
        Assertions.assertEquals(initialDescription, capturedRenovationRecord.getDescription());

        Mockito.doAnswer(invocationOnMock -> Optional.of(renovationRecord))
                .when(renovationRecordRepositorySpy).findById(anyLong());

        Assertions.assertEquals(initialRoomNames.size(),
                capturedRenovationRecord.getRooms().size());
        for (int i = 0; i < initialRoomNames.size(); i++) {
            Assertions.assertEquals(initialRooms.get(i),
                    capturedRenovationRecord.getRooms().get(i));
        }

        RenovationRecord updatedRenovationRecord = new RenovationRecord(updatedName,
                updatedDescription);
        List<Room> updatedRooms = new ArrayList<>();
        for (String roomName : updatedRoomNames) {
            updatedRooms.add(new Room(roomName, updatedRenovationRecord));
        }
        updatedRenovationRecord.addRooms(updatedRooms);
        renovationServiceSpy.save(updatedRenovationRecord);

        ArgumentCaptor<RenovationRecord> updatedRenovationRecordArgumentCaptor = ArgumentCaptor.forClass(
                RenovationRecord.class);
        verify(renovationRecordRepositorySpy, Mockito.atLeast(2))
                .save(updatedRenovationRecordArgumentCaptor.capture());

        RenovationRecord captureUpdatedRenovationRecord = updatedRenovationRecordArgumentCaptor.getValue();
        assertNotNull(captureUpdatedRenovationRecord);
        Assertions.assertEquals(updatedName, captureUpdatedRenovationRecord.getName());
        Assertions.assertEquals(updatedDescription,
                captureUpdatedRenovationRecord.getDescription());

        Assertions.assertEquals(updatedRoomNames.size(),
                captureUpdatedRenovationRecord.getRooms().size());
        for (int i = 0; i < updatedRoomNames.size(); i++) {
            Assertions.assertEquals(updatedRooms.get(i).getName(),
                    captureUpdatedRenovationRecord.getRooms().get(i).getName());
        }
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecords")
    public void validateUniqueRenovationRecordName_Pass(String name) {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        Mockito.doAnswer(invocation -> null)
                .when(renovationRecordRepositorySpy).findRenovationByName(Mockito.any());

        RenovationService renovationService = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);

        Assertions.assertEquals("", renovationService.validateUniqueRenovationRecord(name));
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecords")
    public void validateUniqueRenovationRecordName_Fail(String name) {
        RenovationRecordRepository renovationRecordRepositorySpy = Mockito.mock(
                RenovationRecordRepository.class);
        Mockito.doAnswer(invocation -> new RenovationRecord(invocation.getArgument(0),
                        "Description")).when(renovationRecordRepositorySpy)
                .findRenovationByName(Mockito.any());

        RenovationService renovationService = new RenovationService(
                renovationRecordRepositorySpy, tagService, roomRepository, tagRepository,
                sceneModelService, sceneTextureService, userService);

        Assertions.assertEquals("Renovation record name is not unique", renovationService
                .validateUniqueRenovationRecord(name));
    }

    @Test
    public void nullRoomId_getRoomById_returnsNull() {
        RenovationService renovationService = new RenovationService(renovationRecordRepository,
                tagService, roomRepository, tagRepository, sceneModelService, sceneTextureService, userService);

        assertNull(renovationService.getRoomById(null));
    }

    @Test
    public void nonNullRoomId_getRoomById_callsRepository() {
        RenovationService renovationService = new RenovationService(renovationRecordRepository,
                tagService, roomRepository, tagRepository, sceneModelService, sceneTextureService, userService);
        when(roomRepository.findById(anyLong())).thenReturn(
                Optional.of(new Room("Room", new RenovationRecord())));

        assertNotNull(renovationService.getRoomById(1L));
        verify(roomRepository, atLeastOnce()).findById(anyLong());
    }

    @Test
    public void nonNullRoomIdDoesntExist_getRoomById_returnsNull() {
        RenovationService renovationService = new RenovationService(renovationRecordRepository,
                tagService, roomRepository, tagRepository, sceneModelService, sceneTextureService, userService);
        when(roomRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertNull(renovationService.getRoomById(1L));
        verify(roomRepository, atLeastOnce()).findById(anyLong());
    }

}

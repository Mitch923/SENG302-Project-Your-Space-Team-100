package nz.ac.canterbury.seng302.homehelper.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class RenovationRenovationDesignServiceIntegrationTest {

    @Autowired
    RenovationRecordRepository renovationRecordRepository;

    @Autowired
    RenovationDesignRepository renovationDesignRepository;

    @Autowired
    RenovationDesignService renovationDesignService;
    @Autowired
    UserRepository userRepository;
    @SpyBean
    UserService userService;

    private static Stream<RenovationDesign> validDesigns(RenovationRecord renovationRecord) {
        return Stream.of(
                new RenovationDesign("Renovation Record 1", "Renovating Kitchen", renovationRecord),
                new RenovationDesign("Renovation Record 2", "Bathroom upgrade with new tiles",
                        renovationRecord),
                new RenovationDesign("Renovation Record 3", "Adding a new guest bedroom",
                        renovationRecord),
                new RenovationDesign("Renovation Record 4",
                        "Replacing old flooring in the living room",
                        renovationRecord),
                new RenovationDesign("Renovation Record 5", "Complete repainting of the house",
                        renovationRecord),
                new RenovationDesign("Renovation Record 6", "Roof maintenance and repair",
                        renovationRecord),
                new RenovationDesign("Renovation Record 7", "Installing new windows in the office",
                        renovationRecord),
                new RenovationDesign("Renovation Record 8", "Expanding the backyard patio",
                        renovationRecord),
                new RenovationDesign("Renovation Record 9",
                        "Upgrading electrical wiring for safety",
                        renovationRecord),
                new RenovationDesign("Renovation Record 10",
                        "Modernizing the home with smart devices",
                        renovationRecord)
        );
    }

    public static Stream<Arguments> searchPageByIdResults() {
        return Stream.of(
                Arguments.of(2, 10),
                Arguments.of(2, 1),
                Arguments.of(9, 3),
                Arguments.of(8, 4),
                Arguments.of(1, 1),
                Arguments.of(6, 7)

        );
    }

    @AfterEach
    public void tearDown() {
        renovationDesignRepository.deleteAll();
        renovationRecordRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void saveDesign() {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        RenovationRecord renovationRecord = new RenovationRecord("12 Ilam Road",
                "Repaint and fix the wonky floors");
        List<Room> rooms = List.of(new Room("Kitchen", renovationRecord),
                new Room("Bathroom", renovationRecord));
        RenovationDesign renovationDesign = new RenovationDesign("Paint",
                "Put a fresh coat of paint on all the walls",
                renovationRecord);
        renovationRecord.addRooms(rooms);
        renovationRecord.addDesign(renovationDesign);
        renovationRecord.setUser(user);
        renovationRecordRepository.save(renovationRecord);
        RenovationDesign originalRenovationDesign = renovationDesignRepository.save(
                renovationDesign);

        RenovationDesign newRenovationDesign = new RenovationDesign("Modern Art Deco",
                "Put a fresh coat of paint on all the walls",
                renovationRecord);
        renovationDesignService.saveDesignDetails(newRenovationDesign,
                originalRenovationDesign.getId());

        RenovationDesign savedRenovationDesign = renovationDesignRepository.getDesignById(
                originalRenovationDesign.getId());
        assertEquals(savedRenovationDesign.getName(), newRenovationDesign.getName());
        assertEquals(savedRenovationDesign.getDescription(), newRenovationDesign.getDescription());

    }

    @Test
    void whenUpdateTaskIcon_TaskIconIsUpdated() {
        // Arrange
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));

        Mockito.doReturn(user).when(userService).getLoggedUser();

        RenovationRecord renovationRecord = new RenovationRecord("12 Ilam Road",
                "Repaint and fix the wonky floors");
        List<Room> rooms = List.of(new Room("Kitchen", renovationRecord),
                new Room("Bathroom", renovationRecord));
        RenovationDesign renovationDesign = new RenovationDesign("Paint",
                "Put a fresh coat of paint on all the walls",
                renovationRecord,
                rooms.getLast());
        renovationRecord.addRooms(rooms);
        renovationRecord.addDesign(renovationDesign);
        renovationRecord.setUser(user);
        renovationRecordRepository.save(renovationRecord);
        renovationDesign = renovationDesignRepository.save(renovationDesign);
        assertNull(renovationDesign.getIconName());

        // Act
        renovationDesignService.updateDesignIcon(renovationDesign.getId(), "testIconName");
        renovationDesign = renovationDesignRepository.findById(renovationDesign.getId()).get();

        // Assert
        assertEquals("testIconName", renovationDesign.getIconName());
    }

    @Transactional
    @Test
    void designExists_deleteDesign_designNotInRepository() throws IOException {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));

        Mockito.doReturn(user).when(userService).getLoggedUser();

        RenovationRecord renovationRecord = new RenovationRecord("12 Ilam Road",
                "Repaint and fix the wonky floors");
        List<Room> rooms = List.of(new Room("Kitchen", renovationRecord),
                new Room("Bathroom", renovationRecord));
        RenovationDesign renovationDesign = new RenovationDesign("Paint",
                "Put a fresh coat of paint on all the walls",
                renovationRecord,
                rooms.getLast());
        renovationRecord.addRooms(rooms);
        renovationRecord.addDesign(renovationDesign);
        renovationRecord.setUser(user);
        // Provide a url so there isn't a null exception but deleting files not tested in this test
        renovationDesign.setSceneChunkDirectory("/uploads/scenes/design_id4");
        renovationRecordRepository.save(renovationRecord);
        renovationDesign = renovationDesignRepository.save(renovationDesign);
        assertEquals("Paint",
                renovationDesignRepository.getDesignById(renovationDesign.getId()).getName());

        renovationDesignService.deleteDesign(renovationDesign.getId(), renovationRecord.getId());

        assertNull(renovationDesignRepository.getDesignById(renovationDesign.getId()));
    }

    @Transactional
    @Test
    void designFileExists_deleteDesign_designNotOnDisk() throws IOException {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));

        Mockito.doReturn(user).when(userService).getLoggedUser();

        RenovationRecord renovationRecord = new RenovationRecord("12 Ilam Road",
                "Repaint and fix the wonky floors");
        List<Room> rooms = List.of(new Room("Kitchen", renovationRecord),
                new Room("Bathroom", renovationRecord));
        RenovationDesign renovationDesign = new RenovationDesign("Paint",
                "Put a fresh coat of paint on all the walls",
                renovationRecord,
                rooms.getLast());
        renovationRecord.addRooms(rooms);
        renovationRecord.addDesign(renovationDesign);
        renovationRecord.setUser(user);
        renovationRecordRepository.save(renovationRecord);
        renovationDesign = renovationDesignRepository.save(renovationDesign);
        assertEquals("Paint",
                renovationDesignRepository.getDesignById(renovationDesign.getId()).getName());

        File directory = new File("test-uploads/scenes/design_id" + renovationDesign.getId());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, "chunk-0");
        file.createNewFile();
        renovationDesign.setSceneChunkDirectory("/test-uploads/scenes/" + directory.getName());

        renovationDesignService.deleteDesign(renovationDesign.getId(), renovationRecord.getId());

        File fileExists = new File(directory, "design_id" + renovationDesign.getId() + ".glb");
        assertFalse(fileExists.exists());
    }

    @ParameterizedTest
    @MethodSource("searchPageByIdResults")
    void searchQueryCheckPageByIdReturnsCorrectPage(long designId, int pageSize) {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));

        Mockito.doReturn(user).when(userService).getLoggedUser();
        RenovationRecord renovationRecord = new RenovationRecord("12 Ilam Road",
                "Repaint and fix the wonky floors");
        renovationRecord.setUser(user);
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        validDesigns(renovationRecord).forEach(design -> {
            renovationDesignService.createDesign(design);
        });
        List<RenovationDesign> renovationDesignList = new ArrayList<>();
        renovationDesignRepository.findAll().forEach(design -> {
            renovationDesignList.add(design);
        });
        long offset = renovationDesignRepository.findAll().iterator().next().getId();
        long actualId = designId + offset - 1;
        int actualPage = renovationDesignService.findPageNumberOfId(actualId, pageSize,
                renovationRecord.getId());
        List<RenovationDesign> renovationDesigns = renovationDesignService.getDesignPageForRenovation(
                renovationRecord,
                actualPage, pageSize).getContent();
        assert renovationDesigns.stream().anyMatch(design -> design.getId() == actualId);
    }
}

package nz.ac.canterbury.seng302.homehelper.integration.service;

import static org.mockito.Mockito.when;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class RenovationServiceIntegrationTest {

    @Autowired
    @InjectMocks
    private RenovationService renovationService;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private UserRepository userRepository;

    private User loggedUser = new User("jane@example.com", "password", "Jane", "Doe");
    private User otherUser = new User("john@example.com", "password", "John", "Doe");

    @MockBean
    private UserService userService;

    @BeforeEach
    void beforeEach() {
        loggedUser = userRepository.save(loggedUser);
        otherUser = userRepository.save(otherUser);

        when(userService.getLoggedUser()).thenReturn(loggedUser);
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    List<RenovationRecord> listOfRenos = List.of(
            new RenovationRecord(loggedUser, "Kitchen Renovation", "Complete remodel of kitchen area"),
            new RenovationRecord(loggedUser, "Kitchen Upgrade", "Install new countertops and cabinets"),
            new RenovationRecord(loggedUser, "Outdoor Kitchen Build", "Add an outdoor cooking area"),
            new RenovationRecord(loggedUser, "Kitchen Painting", "Repaint kitchen walls and ceiling"),
            new RenovationRecord(loggedUser, "Bathroom Remodel", "New tiles and shower install")
    );


    List<RenovationRecord> listOfRenosOtherUser = List.of(
            new RenovationRecord(otherUser, "Kitchen Renovation", "Complete remodel of kitchen area"),
            new RenovationRecord(otherUser, "Kitchen Upgrade", "Install new countertops and cabinets"),
            new RenovationRecord(otherUser, "Outdoor Kitchen Build", "Add an outdoor cooking area"),
            new RenovationRecord(otherUser, "Kitchen Painting", "Repaint kitchen walls and ceiling"),
            new RenovationRecord(otherUser, "Bathroom Remodel", "New tiles and shower install")
    );


    @Test
    void getMatchingRenovationRecordsByPartialString_MultipleRecordsMatch_ListReturned() {
        renovationRecordRepository.saveAll(listOfRenos);
        String searchString = "Kitchen";
        List<RenovationRecord> renovations = renovationService.getRenovationRecordsByNameSubstring(searchString);
        List<String> names = renovations.stream().map(RenovationRecord::getName).toList();
        Assertions.assertTrue(names.containsAll(List.of("Kitchen Renovation", "Kitchen Upgrade", "Outdoor Kitchen Build", "Kitchen Painting")));
    }


    @Test
    void getMatchingRenovationRecordsByPartialString_NoRecordsMatch_EmptyListReturned() {
        renovationRecordRepository.saveAll(listOfRenos);
        String searchString = "Garage";
        List<RenovationRecord> renovations = renovationService.getRenovationRecordsByNameSubstring(searchString);
        Assertions.assertTrue(renovations.isEmpty());
    }


    @Test
    void getMatchingRenovationRecordsByPartialString_UserDoesNotOwnRecordsThatMatch_EmptyListReturned() {
        renovationRecordRepository.saveAll(listOfRenosOtherUser);
        String searchString = "Kitchen";
        List<RenovationRecord> renovations = renovationService.getRenovationRecordsByNameSubstring(searchString);
        Assertions.assertTrue(renovations.isEmpty());
    }


}

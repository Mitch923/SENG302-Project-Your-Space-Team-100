package nz.ac.canterbury.seng302.homehelper.integration.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import nz.ac.canterbury.seng302.homehelper.auth.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class DesignServiceIntegrationTests {

    @Autowired
    CompetitionRepository competitionRepository;
    @Autowired
    RenovationDesignService designService;
    @Autowired
    UserService userService;
    @Autowired
    RenovationService renovationService;
    @Autowired
    RenovationDesignRepository designRepository;
    @Autowired
    CompetitionDesignService competitionDesignService;
    @Autowired
    CompetitionDesignRepository competitionDesignRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private CustomAuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User loggedUser;
    private User otherUser;
    @Autowired
    private RenovationDesignService renovationDesignService;


    @BeforeEach
    void setUp() {
        String rawPassword = "P4$$word";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create verified user
        User john = new User("john@email.com", encodedPassword, "John", "Doe");
        userService.saveUser(john);
        userService.verifyUser(john);

        // Authenticate the user
        Authentication authRequest = new UsernamePasswordAuthenticationToken(john.getEmail(),
                rawPassword);
        Authentication authResult = authenticationProvider.authenticate(authRequest);

        SecurityContextHolder.getContext().setAuthentication(authResult);
        loggedUser = john;

        //      Add another user
        User jane = new User("jane@email.com", "DumbPassword", "Jane", "Doe");
        jane.setId(500L);
        userService.saveUser(jane);
        userService.verifyUser(jane);
        otherUser = jane;
    }

    @AfterEach
    void tearDown() {
        designRepository.deleteAll();
        competitionDesignRepository.deleteAll();
        renovationRecordRepository.deleteAll();
        competitionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userHasDesigns_getAllDesignsByUser_correctDesignsReturned() {
        // Give john some renovation records and designs
        RenovationRecord johnsRenoRecord1 = new RenovationRecord(loggedUser, "Test reno1", "");
        RenovationRecord johnsRenoRecord2 = new RenovationRecord(loggedUser, "Test reno2", "");
        renovationService.addRenovationRecord(johnsRenoRecord1, null);
        renovationService.addRenovationRecord(johnsRenoRecord2, null);

        RenovationDesign renovationDesign1 = new RenovationDesign("name", "description",
                johnsRenoRecord1);
        RenovationDesign renovationDesign2 = new RenovationDesign("name", "description",
                johnsRenoRecord1);
        RenovationDesign renovationDesign3 = new RenovationDesign("name", "description",
                johnsRenoRecord2);
        designService.createDesign(renovationDesign1);
        designService.createDesign(renovationDesign2);
        designService.createDesign(renovationDesign3);

        // Test get johns designs
        List<RenovationDesign> results = designService.getAllDesignsByUser();
        Assertions.assertNotNull(results);
        Assertions.assertEquals(3, results.size());
    }

    @Test
    void otherUserHasDesigns_getAllDesignsByUser_correctDesignsReturned() {
        // Give Jane some renovation records and designs
        RenovationRecord janesRenoRecord1 = new RenovationRecord(otherUser, "Test reno1", "");
        RenovationRecord janesRenoRecord2 = new RenovationRecord(otherUser, "Test reno2", "");
        RenovationRecord janesRenoRecord3 = new RenovationRecord(otherUser, "Test reno3", "");
        renovationService.addRenovationRecord(janesRenoRecord1, null);
        renovationService.addRenovationRecord(janesRenoRecord2, null);
        renovationService.addRenovationRecord(janesRenoRecord3, null);

        RenovationDesign renovationDesign1 = new RenovationDesign("name", "description",
                janesRenoRecord1);
        RenovationDesign renovationDesign2 = new RenovationDesign("name", "description",
                janesRenoRecord1);
        RenovationDesign renovationDesign3 = new RenovationDesign("name", "description",
                janesRenoRecord2);
        RenovationDesign renovationDesign4 = new RenovationDesign("name", "description",
                janesRenoRecord3);
        designService.createDesign(renovationDesign1);
        designService.createDesign(renovationDesign2);
        designService.createDesign(renovationDesign3);
        designService.createDesign(renovationDesign4);

        // Test get Johns designs
        List<RenovationDesign> results = designService.getAllDesignsByUser();
        Assertions.assertEquals(0, results.size());
    }

    @Test
    void userHasDesigns_searchDesignsEmptyQuery_correctDesignsReturned() {
        RenovationRecord johnsRenoRecord1 = new RenovationRecord(loggedUser, "Test reno1", "");
        RenovationRecord johnsRenoRecord2 = new RenovationRecord(loggedUser, "Test reno2", "");
        renovationService.addRenovationRecord(johnsRenoRecord1, null);
        renovationService.addRenovationRecord(johnsRenoRecord2, null);

        RenovationDesign renovationDesign1 = new RenovationDesign("name1", "description",
                johnsRenoRecord1);
        RenovationDesign renovationDesign2 = new RenovationDesign("name2", "description",
                johnsRenoRecord1);
        RenovationDesign renovationDesign3 = new RenovationDesign("name3", "description",
                johnsRenoRecord2);
        RenovationDesign renovationDesign4 = new RenovationDesign("name12", "description",
                johnsRenoRecord1);

        List<RenovationDesign> expectedDesigns = List.of(
                designService.createDesign(renovationDesign1),
                designService.createDesign(renovationDesign2),
                designService.createDesign(renovationDesign3),
                designService.createDesign(renovationDesign4)
        );

        Set<Long> expectedIds = expectedDesigns.stream()
                .map(RenovationDesign::getId)
                .collect(Collectors.toSet());

        List<RenovationDesign> actual = renovationDesignService.searchUsersRenovationDesigns("", 0,
                8, List.of()).getContent();

        Set<Long> actualIds = actual.stream()
                .map(RenovationDesign::getId)
                .collect(Collectors.toSet());

        Assertions.assertEquals(expectedIds, actualIds);
    }

    @Test
    void userHasDesigns_searchDesignsQuery_correctDesignsReturned() {
        RenovationRecord johnsRenoRecord1 = new RenovationRecord(loggedUser, "Test reno1", "");
        RenovationRecord johnsRenoRecord2 = new RenovationRecord(loggedUser, "Test reno2", "");
        renovationService.addRenovationRecord(johnsRenoRecord1, null);
        renovationService.addRenovationRecord(johnsRenoRecord2, null);

        RenovationDesign renovationDesign1 = new RenovationDesign("name1", "description",
                johnsRenoRecord1);
        RenovationDesign renovationDesign2 = new RenovationDesign("name2", "description",
                johnsRenoRecord1);
        RenovationDesign renovationDesign3 = new RenovationDesign("name3", "description",
                johnsRenoRecord2);
        RenovationDesign renovationDesign4 = new RenovationDesign("name12", "description",
                johnsRenoRecord1);

        designService.createDesign(renovationDesign2);
        designService.createDesign(renovationDesign3);

        List<RenovationDesign> expectedDesigns = List.of(
                designService.createDesign(renovationDesign1),
                designService.createDesign(renovationDesign4)
        );

        Set<Long> expectedIds = expectedDesigns.stream()
                .map(RenovationDesign::getId)
                .collect(Collectors.toSet());

        List<RenovationDesign> actual = renovationDesignService.searchUsersRenovationDesigns(
                "name1", 0, 8, List.of()).getContent();

        Set<Long> actualIds = actual.stream()
                .map(RenovationDesign::getId)
                .collect(Collectors.toSet());

        Assertions.assertEquals(expectedIds, actualIds);
    }

    @Transactional
    @Test
    void unsubmittedDesign_submitDesign_designSubmitted() {
        Competition comp = new Competition("Comp", LocalDate.now(), LocalDate.now().plusDays(1));
        Competition savedComp = competitionRepository.save(comp);
        CompetitionDesign design = new CompetitionDesign("Design", "Description", "", savedComp,
                loggedUser);
        CompetitionDesign savedDesign = competitionDesignRepository.save(design);
        competitionDesignService.submitCompetitionDesign(savedDesign.getId());
        Assertions.assertTrue(
                competitionDesignRepository.getById(savedDesign.getId()).isSubmitted());
    }
}

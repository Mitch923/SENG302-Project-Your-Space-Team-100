package nz.ac.canterbury.seng302.homehelper.integration.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.ThemeService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.DesignSortingType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class CompetitionServiceIntegrationTests {

    @Autowired
    CompetitionRepository competitionRepository;
    @Autowired
    CompetitionService competitionService;
    @Autowired
    CompetitionDesignRepository competitionDesignRepository;
    @SpyBean
    UserService userService;
    @MockBean
    ThemeService themeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompetitionDesignService competitionDesignService;

    private Competition competition;

    private void addCompetitionEntriesAndVotes(int numCompetitions) {
        // Create entries and vote for them
        List<User> users = List.of(new User("john@example.com", "P4$$word", "John", "Doe"),
                new User("jane@example.com", "P4$$word", "Jane", "Doe"),
                new User("alice@example.com", "P4$$word", "Alice", "Doe"),
                new User("bob@example.com", "P4$$word", "Bob", "Doe"),
                new User("hannah@example.com", "P4$$word", "Hannah", "Doe"));
        userRepository.saveAll(users);

        List<CompetitionDesign> entries = new ArrayList<>();
        for (int i = 0; i < numCompetitions; i++) {
            entries.add(
                    new CompetitionDesign("Design " + i, "", "", competition, users.get(i)));
        }

        for (int i = 0; i < entries.size(); i++) {
            competition.addEntry(entries.get(i));
            entries.get(i).setSubmitted(true);
            CompetitionDesign competitionDesign = entries.get(i);
            for (int j = 0; j < i; j++) {
                competitionDesign.incrementVotes(users.get(j));
            }
        }
        competitionService.save(competition);
    }

    @BeforeEach
    void setUp() {
        // Create verified user
        User john = new User("john@email.com", "P4$$word", "John", "Doe");
        userService.saveUser(john);
        doReturn(john).when(userService).getLoggedUser();
        LocalDate today = LocalDate.now();

        LocalDate monday = today
                .minusWeeks(1L)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate sunday = monday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        String theme = "Test Competition";
        competition = competitionRepository.save(new Competition(theme, monday, sunday));
    }

    @AfterEach
    void tearDown() {
        competitionDesignRepository.deleteAll();
        competitionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void competitionExists_getCompetitionById_returnsCompetition() {
        Competition savedCompetition = competitionService.save(
                new Competition("Test Competition", LocalDate.of(2025, 8, 25),
                        LocalDate.of(2025, 8, 31)));
        Competition competition = competitionService.getCompetitionById(savedCompetition.getId());
        assertEquals(savedCompetition.getTheme(), competition.getTheme());
    }

    @Test
    void competitionDoesntExist_getCompetitionById_returnsNull() {
        Competition competition = competitionService.getCompetitionById(155L);
        Assertions.assertNull(competition);
    }

    @Test
    void themeGenerated_createNewCompetition_returnsNewCompetition() {
        Mockito.when(themeService.generateTheme()).thenReturn("Bountiful South American Kitchen");
        competitionService.createNewCompetition();
        Competition competition = competitionService.getCurrentCompetition();
        assertEquals("Bountiful South American Kitchen", competition.getTheme());
    }

    @Test
    void userNotEnteredInComp_createNewCompetitionEntry_entryCreated() {
        LocalDate endDate = LocalDate.now().plusDays(1);
        competitionService.save(new Competition("Test Competition", LocalDate.of(2025, 8, 25),
                endDate));

        CompetitionDesign result = competitionService.createNewCompetitionEntry();
        Assertions.assertNotNull(result);
    }

    @Test
    void userAlreadyEnteredInComp_createNewCompetitionEntry_entryNotCreated() {
        LocalDate endDate = LocalDate.now().plusDays(1);
        competitionService.save(new Competition("Test Competition", LocalDate.of(2025, 8, 25),
                endDate));
        CompetitionDesign result = competitionService.createNewCompetitionEntry();
        Assertions.assertNotNull(result);

        CompetitionDesign result2 = competitionService.createNewCompetitionEntry();
        Assertions.assertNull(result2);
    }

    @Test
    void validDesignToVoteOn_validateToggleVote_returnsTrue() {
        User josh = new User("josh@email.com", "P4$$word", "josh", "Doe");
        userService.saveUser(josh);

        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition currentCompetition = competitionService.save(
                new Competition("Test Competition", startDate, endDate));

        CompetitionDesign competitionDesign = new CompetitionDesign("Design", "", "",
                currentCompetition,
                josh);
        competitionDesign.setSubmitted(true);
        competitionDesign = competitionDesignService.saveCompetitionEntry(competitionDesign);

        assertTrue(competitionService.validateToggleVote(competitionDesign.getId()));
    }

    @Test
    void userOwnsDesign_validateToggleVote_returnsFalse() {
        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        competitionService.save(
                new Competition("Test Competition", startDate, endDate));

        CompetitionDesign competitionDesign = competitionService.createNewCompetitionEntry();

        Assertions.assertFalse(competitionService.validateToggleVote(competitionDesign.getId()));
    }

    @Test
    void designNotEnteredInCurrentComp_validateToggleVote_returnsFalse() {
        User josh = new User("josh@email.com", "P4$$word", "josh", "Doe");
        userService.saveUser(josh);

        //Create Past comp
        LocalDate startDate = LocalDate.now().minusWeeks(2);
        LocalDate endDate = LocalDate.now().minusWeeks(1);
        Competition currentCompetition = competitionService.save(
                new Competition("Test Competition", startDate, endDate));

        //Add entry to da past comp
        CompetitionDesign competitionDesign = new CompetitionDesign("Design", "", "",
                currentCompetition,
                josh);
        competitionDesign = competitionDesignService.saveCompetitionEntry(competitionDesign);

        //Create new comp
        Mockito.when(themeService.generateTheme()).thenReturn("Bountiful South American Kitchen");
        competitionService.createNewCompetition();

        Assertions.assertFalse(competitionService.validateToggleVote(competitionDesign.getId()));
    }

    @Test
    void designNotSubmittedForVoting_validateToggleVote_returnsFalse() {
        User josh = new User("josh@email.com", "P4$$word", "josh", "Doe");
        userService.saveUser(josh);

        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition currentCompetition = competitionService.save(
                new Competition("Test Competition", startDate, endDate));

        CompetitionDesign competitionDesign = new CompetitionDesign("Design", "", "",
                currentCompetition,
                josh);
        competitionDesign = competitionDesignService.saveCompetitionEntry(competitionDesign);

        Assertions.assertFalse(competitionService.validateToggleVote(competitionDesign.getId()));
    }


    @Test
    @Transactional
    void notVoted_toggleVote_voteAdded() {
        User josh = new User("josh@email.com", "P4$$word", "josh", "Doe");
        userService.saveUser(josh);

        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition currentCompetition = competitionService.save(
                new Competition("Test Competition", startDate, endDate));

        CompetitionDesign competitionDesign = new CompetitionDesign("Design", "", "",
                currentCompetition,
                josh);
        competitionDesign = competitionDesignService.saveCompetitionEntry(competitionDesign);

        competitionService.toggleVote(competitionDesign.getId());
        assertEquals(1, competitionDesign.getNumberOfVotes());
    }

    @Test
    @Transactional
    void voted_toggleVote_voteRemoved() {
        User josh = new User("josh@email.com", "P4$$word", "josh", "Doe");
        userService.saveUser(josh);

        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition currentCompetition = competitionService.save(
                new Competition("Test Competition", startDate, endDate));

        CompetitionDesign competitionDesign = new CompetitionDesign("Design", "", "",
                currentCompetition,
                josh);
        competitionDesign = competitionDesignService.saveCompetitionEntry(competitionDesign);

        competitionService.toggleVote(competitionDesign.getId());
        assertEquals(1, competitionDesign.getNumberOfVotes());

        competitionService.toggleVote(competitionDesign.getId());
        assertEquals(0, competitionDesign.getNumberOfVotes());
    }

    @Test
    void competitionHasEntries_getTopCompetitionDesignsByCompetition_returnsCompetitionsWithMostVotes() {
        addCompetitionEntriesAndVotes(5);

        CompetitionDesign[] topEntries = competitionService.getTopCompetitionDesignsByCompetition(
                competition.getId(), 3);

        assertEquals("Design 4", topEntries[0].getName());
        assertEquals("Design 3", topEntries[1].getName());
        assertEquals("Design 2", topEntries[2].getName());
    }

    @Test
    void competitionHasLessEntriesThanRequested_getTopCompetitionDesignsByCompetition_returnsNullPaddedArray() {
        addCompetitionEntriesAndVotes(3);

        CompetitionDesign[] topEntries = competitionService.getTopCompetitionDesignsByCompetition(
                competition.getId(), 5);

        assertEquals("Design 2", topEntries[0].getName());
        assertEquals("Design 1", topEntries[1].getName());
        assertEquals("Design 0", topEntries[2].getName());
        assertNull(topEntries[3]);
        assertNull(topEntries[4]);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9})
    void competitionHasNoDesigns_getTopCompetitionDesignsByCompetition_returnsNullArray(
            int numberOfEntries) {
        CompetitionDesign[] topEntries = competitionService.getTopCompetitionDesignsByCompetition(
                competition.getId(), numberOfEntries);

        assertEquals(numberOfEntries, topEntries.length);
        assertArrayEquals(new CompetitionDesign[numberOfEntries], topEntries);
    }

    @Test
    void sortByVoteAsc_getCompetitionDesignsPage_correctOrder() {
        CompetitionDesign design1 = new CompetitionDesign("Top votes", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design2 = new CompetitionDesign("Medium votes", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design3 = new CompetitionDesign("No votes", "", "", competition,
                userService.getLoggedUser());
        design1.setVoteCount(3);
        design2.setVoteCount(2);
        design3.setVoteCount(0);
        design1.setSubmitted(true);
        design2.setSubmitted(true);
        design3.setSubmitted(true);
        competition.addEntry(design1);
        competition.addEntry(design2);
        competition.addEntry(design3);
        competitionRepository.save(competition);

        List<CompetitionDesign> results = competitionService.getCompetitionDesignsPage(
                competition.getId(),
                0, 8, DesignSortingType.VOTES_ASC).getContent();
        assertEquals(3, results.size());
        assertEquals(0, results.get(0).getVoteCount());
        assertEquals(2, results.get(1).getVoteCount());
        assertEquals(3, results.get(2).getVoteCount());
    }

    @Test
    void sortByVoteDsc_getCompetitionDesignsPage_correctOrder() {
        CompetitionDesign design1 = new CompetitionDesign("Top votes", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design2 = new CompetitionDesign("Medium votes", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design3 = new CompetitionDesign("No votes", "", "", competition,
                userService.getLoggedUser());
        design1.setVoteCount(3);
        design2.setVoteCount(2);
        design3.setVoteCount(0);
        design1.setSubmitted(true);
        design2.setSubmitted(true);
        design3.setSubmitted(true);
        competition.addEntry(design1);
        competition.addEntry(design2);
        competition.addEntry(design3);
        competitionRepository.save(competition);

        List<CompetitionDesign> results = competitionService.getCompetitionDesignsPage(
                competition.getId(),
                0, 8, DesignSortingType.VOTES_DESC).getContent();
        assertEquals(3, results.size());
        assertEquals(3, results.get(0).getVoteCount());
        assertEquals(2, results.get(1).getVoteCount());
        assertEquals(0, results.get(2).getVoteCount());
    }

    @Test
    void sortByNameAsc_getCompetitionDesignsPage_correctOrder() {
        CompetitionDesign design1 = new CompetitionDesign("ABC", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design2 = new CompetitionDesign("DEF", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design3 = new CompetitionDesign("GHI", "", "", competition,
                userService.getLoggedUser());
        design1.setSubmitted(true);
        design2.setSubmitted(true);
        design3.setSubmitted(true);
        competition.addEntry(design1);
        competition.addEntry(design2);
        competition.addEntry(design3);
        competitionRepository.save(competition);

        List<CompetitionDesign> results = competitionService.getCompetitionDesignsPage(
                competition.getId(),
                0, 8, DesignSortingType.NAME_ASC).getContent();
        assertEquals(3, results.size());
        assertEquals("ABC", results.get(0).getName());
        assertEquals("DEF", results.get(1).getName());
        assertEquals("GHI", results.get(2).getName());
    }

    @Test
    void sortByNameDesc_getCompetitionDesignsPage_correctOrder() {
        CompetitionDesign design1 = new CompetitionDesign("ABC", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design2 = new CompetitionDesign("DEF", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design3 = new CompetitionDesign("GHI", "", "", competition,
                userService.getLoggedUser());
        design1.setSubmitted(true);
        design2.setSubmitted(true);
        design3.setSubmitted(true);
        competition.addEntry(design1);
        competition.addEntry(design2);
        competition.addEntry(design3);
        competitionRepository.save(competition);

        List<CompetitionDesign> results = competitionService.getCompetitionDesignsPage(
                competition.getId(),
                0, 8, DesignSortingType.NAME_DESC).getContent();
        assertEquals(3, results.size());
        assertEquals("GHI", results.get(0).getName());
        assertEquals("DEF", results.get(1).getName());
        assertEquals("ABC", results.get(2).getName());

    }


}

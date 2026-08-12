package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.ThemeService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CompetitionServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;
    @Mock
    private ThemeService themeService;
    @Mock
    private UserService userService;
    @Mock
    private CompetitionDesignService competitionDesignService;
    @Mock
    private CompetitionDesignRepository competitionDesignRepository;
    @Mock
    private RenovationDesignService renovationDesignService;

    private CompetitionService competitionService;

    private Competition competition;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        competitionService = new CompetitionService(competitionRepository, themeService,
                userService, competitionDesignRepository, renovationDesignService,
                competitionDesignService);
        competition = new Competition();
        competition.setId(1L);
        when(competitionRepository.getCurrentCompetition()).thenReturn(competition);
        when(competitionDesignRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void userAlreadyHasEntry_createNewCompetitionEntry_returnsNull() {
        User user = new User("", "", "", "");
        user.setId(1L);
        CompetitionDesign existingCompetitionDesign = new CompetitionDesign();
        when(userService.getLoggedUser()).thenReturn(user);
        when(competitionDesignRepository.getByCompetitionIdAndUserId(1L, 1L)).thenReturn(
                existingCompetitionDesign);

        CompetitionDesign newEntry = competitionService.createNewCompetitionEntry();

        assertNull(newEntry);
    }

    @Test
    void userDoesntHaveEntry_createNewCompetitionEntry_returnsNewCompetitionEntry() {
        User user = new User("", "", "Jimmy", "");
        user.setId(1L);
        when(userService.getLoggedUser()).thenReturn(user);
        when(competitionDesignRepository.getByCompetitionIdAndUserId(1L, 1L)).thenReturn(null);

        CompetitionDesign newEntry = competitionService.createNewCompetitionEntry();

        assertNotNull(newEntry);
        assertEquals("Jimmy's Design", newEntry.getName());
    }
}

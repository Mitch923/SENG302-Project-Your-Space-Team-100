package nz.ac.canterbury.seng302.homehelper.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import nz.ac.canterbury.seng302.homehelper.controller.CurrentCompetitionController;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

public class CurrentCompetitionControllerTest {

    @Mock
    private CompetitionService competitionService;

    private CurrentCompetitionController currentCompetitionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currentCompetitionController = new CurrentCompetitionController(competitionService);
    }

    @Test
    public void nullNewCompetition_createCompetitionEntry_throwsException() {
        when(competitionService.createNewCompetitionEntry()).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> currentCompetitionController.createCompetitionEntry());
    }

    @Test
    public void newCompetitionEntry_createCompetitionEntry_returnsRedirect() {
        CompetitionDesign competitionDesign = new CompetitionDesign();
        competitionDesign.setId(1L);
        when(competitionService.createNewCompetitionEntry()).thenReturn(competitionDesign);

        String view = currentCompetitionController.createCompetitionEntry();

        assertEquals("redirect:/editCompetitionEntry/1", view);
    }
}

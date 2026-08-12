package nz.ac.canterbury.seng302.homehelper.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.controller.EditCompetitionDesignController;
import nz.ac.canterbury.seng302.homehelper.dto.DesignDataDTO;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.SceneTextureService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.DesignValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

class EditCompetitionDesignControllerTest {

    @Mock
    private CompetitionDesignService competitionDesignService;
    @Mock
    private UserService userService;
    @Mock
    private SceneModelService sceneModelService;
    @Mock
    private SceneTextureService sceneTextureService;
    @Mock
    private CompetitionService competitionService;

    @Mock
    private Model model;

    private EditCompetitionDesignController editCompetitionDesignController;

    private static Stream<Arguments> invalidHttpCodes() {
        return Stream.of(
                Arguments.of(HttpStatus.BAD_REQUEST),
                Arguments.of(HttpStatus.UNAUTHORIZED),
                Arguments.of(HttpStatus.FORBIDDEN),
                Arguments.of(HttpStatus.NOT_FOUND),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR),
                Arguments.of(HttpStatus.SERVICE_UNAVAILABLE)
        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        editCompetitionDesignController = new EditCompetitionDesignController(
                competitionDesignService, userService, sceneModelService, sceneTextureService,
                competitionService);
    }

    @Test
    void userDoesntOwnCompetitionDesign_postEditCompetitionEntry_returnsForbidden() {
        when(competitionDesignService.userOwnsCompetitionDesign(anyLong())).thenReturn(false);

        ResponseEntity<String> response = editCompetitionDesignController.editCompetitionEntry(1L,
                new DesignDataDTO());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void invalidDesignDetails_postEditCompetitionEntry_returnsBadRequest() {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("error", "error");
        errors.put("message", "message");

        when(competitionDesignService.userOwnsCompetitionDesign(anyLong())).thenReturn(true);
        try (MockedStatic<DesignValidator> ignored = mockStatic(DesignValidator.class)) {
            when(DesignValidator.validateDesignDetails(anyString(), anyString())).thenReturn(
                    errors);

            ResponseEntity<String> response = editCompetitionDesignController.editCompetitionEntry(
                    1L,
                    new DesignDataDTO("", "", ""));

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Test
    void invalidDesignDetails_postEditCompetitionEntry_addsErrorsToBody() {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("error", "error");
        errors.put("message", "message");

        when(competitionDesignService.userOwnsCompetitionDesign(anyLong())).thenReturn(true);
        try (MockedStatic<DesignValidator> ignored = mockStatic(DesignValidator.class)) {
            when(DesignValidator.validateDesignDetails(anyString(), anyString())).thenReturn(
                    errors);

            ResponseEntity<String> response = editCompetitionDesignController.editCompetitionEntry(
                    1L,
                    new DesignDataDTO("", "", ""));

            assertEquals(errors.values().toArray()[0].toString(), response.getBody());
        }
    }

    @Test
    void everythingValid_postEditCompetitionEntry_returnsOK() {
        when(competitionDesignService.userOwnsCompetitionDesign(anyLong())).thenReturn(true);
        try (MockedStatic<DesignValidator> ignored = mockStatic(DesignValidator.class)) {
            when(DesignValidator.validateDesignDetails(anyString(), anyString())).thenReturn(
                    new HashMap<>());

            ResponseEntity<String> response = editCompetitionDesignController.editCompetitionEntry(
                    1L,
                    new DesignDataDTO("", "", ""));

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    void everythingValid_postEditCompetitionEntry_savesCompetitionEntry()
            throws IOException {
        when(competitionDesignService.userOwnsCompetitionDesign(anyLong())).thenReturn(true);
        try (MockedStatic<DesignValidator> ignored = mockStatic(DesignValidator.class)) {
            when(DesignValidator.validateDesignDetails(anyString(), anyString())).thenReturn(
                    new HashMap<>());

            editCompetitionDesignController.editCompetitionEntry(
                    1L,
                    new DesignDataDTO("Name", "Description", ""));

            verify(competitionDesignService).updateCompetitionEntryDetails("Name", "Description",
                    1L);
        }
    }

    @Test
    void userDoesntOwnEntry_getEditCompetitionEntry_throwsForbiddenException() {
        User mockUser = new User("", "", "", "");
        User otherUser = new User("", "", "", "");
        mockUser.setId(1L);
        otherUser.setId(2L);
        CompetitionDesign competitionDesign = new CompetitionDesign();
        competitionDesign.setUser(otherUser);
        when(userService.getLoggedUser()).thenReturn(mockUser);
        when(competitionDesignService.getCompetitionDesignById(anyLong())).thenReturn(
                competitionDesign);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> editCompetitionDesignController.editCompetitionEntry(model, 1L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void entryDoesntExist_getEditCompetitionEntry_throwsNotFound() {
        User mockUser = new User("", "", "", "");
        mockUser.setId(1L);
        when(userService.getLoggedUser()).thenReturn(mockUser);
        when(competitionDesignService.getCompetitionDesignById(anyLong())).thenReturn(
                null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> editCompetitionDesignController.editCompetitionEntry(model, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
